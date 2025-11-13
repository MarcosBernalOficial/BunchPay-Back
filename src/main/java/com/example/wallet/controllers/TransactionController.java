package com.example.wallet.controllers;

import com.example.wallet.controllers.exceptions.UnauthorizedAccessException;
import com.example.wallet.dtos.TransactionDto;
import com.example.wallet.dtos.TransferRequestDto;
import com.example.wallet.model.implementations.AccountClient;
import com.example.wallet.repository.AccountClientRepository;
import com.example.wallet.services.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transacciones", description = "Gestiona las transacciones de la cuenta del usuario")
public class TransactionController {
    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;
    private final AccountClientRepository accountClientRepository;

    public TransactionController(TransactionService transactionService,
            AccountClientRepository accountClientRepository) {
        this.transactionService = transactionService;
        this.accountClientRepository = accountClientRepository;
    }

    @Operation(summary = "Realizar transferencia", description = "Realiza una transferencia")
    @PostMapping("/transfer")
    public ResponseEntity<String> createTransfer(@RequestBody @Valid TransferRequestDto dto, Authentication auth)
            throws Exception {
        String user = auth != null ? auth.getName() : "<anon>";
        String reqId = java.util.UUID.randomUUID().toString();
        log.info("[TX-CTRL] START reqId={} user={} alias={} cvu={} amount={}", reqId, user, dto.getReceiverAlias(),
                dto.getReceiverCVU(), dto.getAmount());
        transactionService.transferMaker(dto, auth);
        log.info("[TX-CTRL] END   reqId={} user={}", reqId, user);
        return ResponseEntity.ok("Transferencia realizada con éxito.");
    }

    @Operation(summary = "Obtener transacciones", description = "Devuelve la lista de transacciones de la cuenta")
    @GetMapping("/viewAll")
    @Transactional(readOnly = true)
    public ResponseEntity<List<TransactionDto>> viewMyTransactions(Authentication auth) {
        String email = auth.getName();

        AccountClient account = accountClientRepository.findByClientEmail(email)
                .orElseThrow(() -> new UnauthorizedAccessException("Sesion expirada. Inicie sesion."));

        List<TransactionDto> transacciones = transactionService.viewAllTransactions(account);
        return ResponseEntity.ok(transacciones);
    }
}
