package com.example.wallet.services;

import com.example.wallet.controllers.exceptions.AccountNotFoundException;
import com.example.wallet.controllers.exceptions.InsufficientBalanceException;
import com.example.wallet.controllers.exceptions.UnauthorizedAccessException;
import com.example.wallet.dtos.TransactionDto;
import com.example.wallet.dtos.TransactionFilterDto;
import com.example.wallet.dtos.TransactionSummaryDto;
import com.example.wallet.dtos.TransferRequestDto;
import com.example.wallet.model.enums.TransactionType;
import com.example.wallet.model.implementations.AccountClient;
import com.example.wallet.model.implementations.Transaction;
import com.example.wallet.repository.AccountClientRepository;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.wallet.model.enums.TransactionType.*;

@Service
@RequiredArgsConstructor
public class TransactionService {
  private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
  private final AccountClientRepository accountClientRepository;
  private final NotificationService notificationService;
  private final PdfService pdfService;
  private final EmailService emailService;

  @Transactional
  public void transferMaker(TransferRequestDto dto, Authentication auth) throws Exception {
    String email = auth.getName();
    String reqId = java.util.UUID.randomUUID().toString();
    log.info("[TX-SVC] START reqId={} user={} alias={} cvu={} amount={}", reqId, email, dto.getReceiverAlias(),
        dto.getReceiverCVU(), dto.getAmount());

    // Getting the accounts
    AccountClient origin = accountClientRepository.findByClientEmail(email)
        .orElseThrow(() -> new UnauthorizedAccessException("Inicio de sesion expirado. Vuelva a iniciar sesion."));

    AccountClient destiny = null;
    log.info("[TX-SVC] reqId={} originAccountId={}", reqId, origin.getId());

    // Check cvu
    if (dto.getReceiverCVU() != null && !dto.getReceiverCVU().isBlank()) {
      destiny = accountClientRepository.findByCvu(dto.getReceiverCVU())
          .orElse(null);
    }

    // Check Alias
    if (destiny == null && dto.getReceiverAlias() != null && !dto.getReceiverAlias().isBlank()) {
      destiny = accountClientRepository.findByAlias(dto.getReceiverAlias())
          .orElse(null);
    }

    // Destiny account not found
    if (destiny == null) {
      log.warn("[TX-SVC] reqId={} destiny not found (alias={}, cvu={})", reqId, dto.getReceiverAlias(),
          dto.getReceiverCVU());
      throw new AccountNotFoundException("No se ha encontrado la cuenta de destino");
    }

    // Balance Validation
    if (origin.getBalance() < dto.getAmount()) {
      log.warn("[TX-SVC] reqId={} insufficient balance originBalance={} amount={}", reqId, origin.getBalance(),
          dto.getAmount());
      throw new InsufficientBalanceException("Saldo insuficiente.");
    }

    // Add and subtract balance
    float originBefore = origin.getBalance();
    float destinyBefore = destiny.getBalance();
    origin.setBalance(origin.getBalance() - dto.getAmount());
    destiny.setBalance(destiny.getBalance() + dto.getAmount());
    log.info("[TX-SVC] reqId={} balances updated origin {} -> {} destiny {} -> {}", reqId, originBefore,
        origin.getBalance(), destinyBefore, destiny.getBalance());

    // Create the transaction para el que envía (RETIRO)
    Transaction originTransaction = new Transaction();
    originTransaction.setDate(LocalDateTime.now());
    originTransaction.setAmount(dto.getAmount());
    originTransaction.setDescription(dto.getDescription());
    originTransaction.setType(RETIRO); // Cambiado a RETIRO
    originTransaction.setSender(origin.getClient());
    originTransaction.setReciever(destiny.getClient());
    originTransaction.setAccount(origin);

    // Create the transaction para el que recibe (DEPOSITO)
    Transaction destinyTransaction = new Transaction();
    destinyTransaction.setDate(LocalDateTime.now());
    destinyTransaction.setAmount(dto.getAmount());
    destinyTransaction.setDescription(dto.getDescription());
    destinyTransaction.setType(DEPOSITO); // Cambiado a DEPOSITO
    destinyTransaction.setSender(origin.getClient());
    destinyTransaction.setReciever(destiny.getClient());
    destinyTransaction.setAccount(destiny);

    // Add to a transaction list
    origin.getTransactionsList().add(originTransaction);
    destiny.getTransactionsList().add(destinyTransaction);

    // Save changes (evitar duplicados: no llamar a transactionRepository.save
    // cuando cascade ya persiste)
    log.info("[TX-SVC] reqId={} persisting entities via cascade...", reqId);
    accountClientRepository.save(origin);
    accountClientRepository.save(destiny);
    // IDs deben estar asignados por cascade al guardar las cuentas
    log.info("[TX-SVC] reqId={} persisted (cascade) originTxId={} destinyTxId={}", reqId, originTransaction.getId(),
        destinyTransaction.getId());

    // --- NOTIFICACIONES ---
    // Para el que envió
    notificationService.createNotification(
        origin.getClient(),
        "Transferencia enviada",
        "Enviaste $" + dto.getAmount() + " a " +
            destiny.getClient().getFirstName() + " " + destiny.getClient().getLastName());

    // Para el que recibió
    notificationService.createNotification(
        destiny.getClient(),
        "Transferencia recibida",
        "Recibiste $" + dto.getAmount() + " de " +
            origin.getClient().getFirstName() + " " + origin.getClient().getLastName());

    // ... código de transferencia ...

    // 1. Generar HTML del comprobante para el destinatario
    log.info("[TX-SVC] reqId={} generating receipt...", reqId);
    String comprobanteHtml = generarComprobanteHtml(destinyTransaction);

    // 2. Generar PDF
    byte[] pdfBytes = pdfService.generatePdfFromHtml(comprobanteHtml);
    log.info("[TX-SVC] reqId={} pdf generated ({} bytes)", reqId, pdfBytes != null ? pdfBytes.length : -1);

    // 3. Enviar PDF al mail del destinatario
    String destinatarioEmail = destiny.getClient().getEmail();
    emailService.sendPdfByEmail(destinatarioEmail, pdfBytes);
    log.info("[TX-SVC] reqId={} sent pdf to destinyEmail={}", reqId, destinatarioEmail);

    // (Opcional) Mandar también al remitente:
    String remitenteEmail = origin.getClient().getEmail();
    emailService.sendPdfByEmail(remitenteEmail, pdfBytes);
    log.info("[TX-SVC] END   reqId={} sent pdf to originEmail={}", reqId, remitenteEmail);
  }

  // Transforms transaction to transactionDto
  private TransactionDto mapToDto(Transaction trans) {
    TransactionDto dto = new TransactionDto();

    dto.setDate(trans.getDate());
    dto.setAmount(trans.getAmount());
    dto.setType(trans.getType());
    dto.setTransactionId(trans.getId());
    dto.setDescription(trans.getDescription());

    // Protegemos sender
    if (trans.getSender() != null) {
      dto.setSenderFirstName(trans.getSender().getFirstName());
      dto.setSenderLastName(trans.getSender().getLastName());
    }
    dto.setSenderCvu(trans.getAccount() != null ? trans.getAccount().getCvu() : null);

    // Protegemos reciever
    if (trans.getReciever() != null) {
      dto.setRecieverFirstName(trans.getReciever().getFirstName());
      dto.setRecieverLastName(trans.getReciever().getLastName());
    }
    dto.setRecieverCvu(trans.getReciever() != null ? dto.getRecieverCvu() : null);

    return dto;
  }

  // To list all transactions
  public List<TransactionDto> viewAllTransactions(AccountClient account) {
    return account.getTransactionsList().stream()
        .map(trans -> mapToDto(trans))
        .collect(Collectors.toList());
  }

  // To filter transactions
  @Transactional
  public List<TransactionDto> getFilteredTransactions(AccountClient account, TransactionFilterDto filter) {
    String email = account.getClient().getEmail();

    return account.getTransactionsList().stream()
        // Filter by type
        .filter(transaction -> filter.getType() == null || transaction.getType() == filter.getType())

        // Filter by month
        .filter(transaction -> {
          if (filter.getMonth() == null)
            return true;
          return transaction.getDate().getMonthValue() == filter.getMonth();
        })

        .map(transaction -> mapToDto(transaction))
        .collect(Collectors.toList());
  }

  /*
   * //Summary transaction
   * 
   * @Transactional
   * public TransactionSummaryDto getMonthlySummary(AccountClient account, int
   * month, int year) {
   * 
   * TransactionSummaryDto dto = new TransactionSummaryDto();
   * dto.setMonth(month);
   * dto.setYear(year);
   * 
   * List<Transaction> filtered = account.getTransactionsList().stream()
   * .filter(t -> t.getDate().getMonthValue() == month &&
   * t.getDate().getYear() == year)
   * .toList();
   * 
   * //Total of expences
   * BigDecimal totalExpenses = filtered.stream()
   * .filter(t -> t.getType() == RETIRO||
   * t.getType() == sube ||
   * t.getType() == celular ||
   * t.getType() == steam ||
   * t.getType() == PAGO)
   * .map(t -> BigDecimal.valueOf(t.getAmount()))
   * .reduce(BigDecimal.ZERO, BigDecimal::add);
   * 
   * dto.setTotalExpenses(totalExpenses);
   * 
   * //Total per category
   * Map<String, BigDecimal> categoryTotals = filtered.stream()
   * .filter(t -> t.getType() == T)
   * 
   * }
   */

  // Generate receipt with HTML and CSS
  public String generarComprobanteHtml(Transaction tr) {

    TransactionDto tx = mapToDto(tr);
    // Formato fecha/hora bonito
    String fecha = tx.getDate() != null ? tx.getDate().toString().replace("T", " ") : "-";

    return """
        <html>
        <head>
          <meta charset="UTF-8" />
          <style>
            body {
              font-family: 'Segoe UI', 'Roboto', Arial, sans-serif;
              background: #F5F7FB;
              color: #232946;
              margin: 0 auto;
              padding: 0;
            }
            .comprobante-container {
              background: #fff;
              border-radius: 16px;
              box-shadow: 0 8px 30px rgba(44,62,80,.12);
              max-width: 420px;
              margin: 40px auto;
              padding: 32px 24px 28px 24px;
              border: 1.5px solid #415a77;
            }
            .titulo {
              color: #0074D9;
              text-align: center;
              font-size: 2rem;
              font-weight: bold;
              margin-bottom: 18px;
            }
            .dato {
              margin: 10px 0;
              font-size: 1.13rem;
              line-height: 1.4;
            }
            .monto {
              font-size: 2rem;
              color: #14B86B;
              font-weight: bold;
              text-align: center;
              margin: 18px 0 12px 0;
            }
            .detalles {
              font-size: 0.98rem;
              margin-top: 12px;
              color: #495057;
            }
            .footer {
              text-align: center;
              margin-top: 32px;
              font-size: 0.95rem;
              color: #a3b5cc;
            }
          </style>
        </head>
        <body>
          <div class="comprobante-container">
            <div class="titulo">Comprobante de Transferencia</div>
            <div class="monto">$ %s</div>
            <div class="dato"><b>Fecha y Hora:</b> %s</div>
            <div class="dato"><b>Remitente:</b> %s %s</div>
            <div class="dato"><b>CVU Remitente:</b> %s</div>
            <div class="dato"><b>Destinatario:</b> %s %s</div>
            <div class="dato"><b>Descripción:</b> %s</div>
            <div class="footer">Gracias por usar BunchPay</div>
          </div>
        </body>
        </html>
        """.formatted(
        String.format("%.2f", tx.getAmount()),
        fecha,
        tx.getSenderFirstName(), tx.getSenderLastName(),
        tx.getSenderCvu(),
        tx.getRecieverFirstName(), tx.getRecieverLastName(),
        tx.getDescription());
  }

}
