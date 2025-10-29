package com.example.wallet.services;

import com.example.wallet.config.EncryptionUtil;
import com.example.wallet.controllers.exceptions.ResourceNotFoundException;
import com.example.wallet.dtos.CardDto;
import com.example.wallet.model.enums.CardType;
import com.example.wallet.model.implementations.AccountClient;
import com.example.wallet.model.implementations.Card;
import com.example.wallet.model.implementations.Client;
import com.example.wallet.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    public Card generateDefaultCard(Client client, AccountClient account) {
        Card card = new Card();
        card.setOwnerName(client.getFirstName() + " " + client.getLastName());
        card.setBank("Bunch Pay");
        card.setIdentifier(generateCardNumber());
        card.setCvv(EncryptionUtil.encrypt(generateCvv()));
        card.setType(CardType.OTHER);
        card.setValidFrom(LocalDate.now());
        card.setExpirationDate(LocalDate.now().plusYears(5));
        card.setClient(client);
        return card;
    }

    private String generateCardNumber() {
        // generates 16 digits, starts with 4
        return "4" + new Random().ints(15, 0, 10)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining());
    }

    private String generateCvv() {
        return "%03d".formatted(new Random().nextInt(1000));
    }

    private String maskCardNumber(String number) {
        return "**** **** **** " + number.substring(number.length() - 4);
    }

    // To transform a card to dto
    private CardDto toDto(Card card) {
        CardDto dto = new CardDto();

        dto.setCardHolderName(card.getOwnerName());
        dto.setCardNumber(card.getIdentifier());
        dto.setMaskedCardNumber(maskCardNumber(card.getIdentifier()));
        dto.setExpirationDate(card.getExpirationDate());
        dto.setCvv(EncryptionUtil.decrypt(card.getCvv()));

        return dto;
    }

    public CardDto getOneCard(String email) {
        Card card = cardRepository.findByClientEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontro la tarjeta."));

        return toDto(card);
    }

}
