package com.example.wallet.services;

import com.example.wallet.dtos.AccountSummaryDto;
import com.example.wallet.dtos.AliasChangeDto;
import com.example.wallet.model.implementations.AccountClient;
import com.example.wallet.model.implementations.Card;
import com.example.wallet.model.implementations.Client;
import com.example.wallet.repository.AccountClientRepository;
import com.example.wallet.utils.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class AccountClientService {

    @Autowired
    AccountClientRepository accountClientRepository;
    @Autowired
    CardService cardService;

    public AccountClient createAccountClient (Client c) {
        AccountClient account = new AccountClient();
        account.setClient(c);
        account.setTransactionsList(new ArrayList<>());
        account.setServicesList(new ArrayList<>());
        account.setCardList(new ArrayList<>());
        account.setBalance(0);
        account.setAlias(c.getFirstName() + ".BunchPay." + IdGenerator.generate());
        account.setCvu(IdGenerator.generateNumericId(22));

        //Card generator
        Card card = cardService.generateDefaultCard(c, account);
        account.setCardList(List.of(card));

        return accountClientRepository.save(account);
    }


    public float viewBalance(String email) {
        return accountClientRepository.findByClientEmail(email)
                .map(AccountClient::getBalance)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontró cuenta para el usuario: " + email
                ));

    }

    public void changeAlias(String email, AliasChangeDto dto) {
        AccountClient accountClient = accountClientRepository.findByClientEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No se encontró cuenta para el usuario: " + email
                ));

        if (accountClient.getAlias().equalsIgnoreCase(dto.getNewAlias())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nuevo alias es igual al actual.");
        }

        if (accountClientRepository.existsByAlias(dto.getNewAlias())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El alias ya está en uso.");
        }
        accountClient.setAlias(dto.getNewAlias());
        accountClientRepository.save(accountClient);
    }


    public String getAlias(String email){
        AccountClient account = accountClientRepository.findByClientEmail(email)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        return account.getAlias();
    }

    public String getCvu(String email){
        AccountClient account = accountClientRepository.findByClientEmail(email)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
        return account.getCvu();
    }

    public AccountSummaryDto getAccountSummary(String email){
        AccountClient account = accountClientRepository.findByClientEmail(email)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));

        AccountSummaryDto dto = new AccountSummaryDto();
        dto.setBalance(account.getBalance());
        dto.setAlias(account.getAlias());
        dto.setCvu(account.getCvu());
        return dto;
    }
}

