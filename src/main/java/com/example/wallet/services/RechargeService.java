package com.example.wallet.services;

import com.example.wallet.controllers.exceptions.InsufficientBalanceException;
import com.example.wallet.controllers.exceptions.InvalidInputException;
import com.example.wallet.controllers.exceptions.UnauthorizedAccessException;
import com.example.wallet.dtos.RechargeRequestDto;
import com.example.wallet.model.enums.TransactionType;
import com.example.wallet.model.implementations.AccountClient;
import com.example.wallet.model.implementations.Transaction;
import com.example.wallet.repository.AccountClientRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class RechargeService {
  private final AccountClientRepository accountClientRepository;
  private final NotificationService notificationService;
  private final PdfService pdfService;
  private final EmailService emailService;

  // Validates the destination
  public void destinationValidation(String type, String destination) {
    if ("SUBE".equalsIgnoreCase(type)) {
      if (!destination.matches("^\\d{16}$")) {
        throw new InvalidInputException("Ingrese una tarjeta de SUBE valida.");
      }
    } else if ("CELULAR".equalsIgnoreCase(type)) {
      if (!destination.matches("^\\d{10,13}$")) {
        throw new InvalidInputException("Ingrese un celular valido.");
      }
    } else if ("STEAM".equalsIgnoreCase(type)) {
      if (!destination.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
        throw new InvalidInputException("El email ingresado no es valido.");
      }
    } else {
      throw new InvalidInputException("Tipo de destino no reconocido.");
    }
  }

  public void processRecharge(RechargeRequestDto dto, String email) throws Exception {
    AccountClient acc = accountClientRepository.findByClientEmail(email)
        .orElseThrow(() -> new UnauthorizedAccessException(email));

    destinationValidation(dto.getType(), dto.getDestination());

    if (acc.getBalance() < dto.getAmount()) {
      throw new InsufficientBalanceException("Saldo insuficiente");
    }

    acc.setBalance(acc.getBalance() - dto.getAmount());
    accountClientRepository.save(acc);

    // Create the transaction
    Transaction trans = new Transaction();
    trans.setAmount(dto.getAmount());
    trans.setDate(LocalDateTime.now());
    trans.setDescription("Recarga de servicio: " + dto.getType());
    trans.setType(TransactionType.PAGO);
    trans.setReciever(null);
    trans.setSender(acc.getClient());
    trans.setAccount(acc);

    acc.getTransactionsList().add(trans);
    accountClientRepository.save(acc);

    notificationService.createNotification(
        acc.getClient(),
        "Carga realizada",
        "Carga exitosa de $%s a %s: %s".formatted(dto.getAmount(), dto.getType(), dto.getDestination()));

    // 1. Generar HTML del comprobante
    String comprobanteHtml = generarComprobanteHtml(trans);

    // 2. Generar PDF
    byte[] pdfBytes = pdfService.generatePdfFromHtml(comprobanteHtml);

    // 3. Enviar PDF al mail
    emailService.sendPdfByEmail(email, pdfBytes);
  }

  // Generate receipt with HTML and CSS
  public String generarComprobanteHtml(Transaction tx) {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    String fechaFormateada = tx.getDate().format(formatter);
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
              <div class="titulo">Comprobante de Recarga</div>
              <div class="monto">$ %s</div>
              <div class="dato"><b>Fecha y Hora:</b> %s</div>
              <div class="dato">%s</div>
              <div class="footer"><b>Gracias por usar BunchPay</b></div>
            </div>
          </body>
        </html>
        """.formatted(
        String.format("%.2f", tx.getAmount()),
        fechaFormateada,
        tx.getDescription());
  }
}
