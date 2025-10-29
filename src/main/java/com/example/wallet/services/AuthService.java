package com.example.wallet.services;

import com.example.wallet.dtos.LoginUserDto;
import com.example.wallet.dtos.RegisterUserDto;
import com.example.wallet.model.implementations.*;
import com.example.wallet.repository.ClientRepository;
import com.example.wallet.repository.SupportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private SupportRepository supportRepository;

    @Autowired
    private ClientService clientService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public AccountClient registerClient(RegisterUserDto dto) {
        return clientService.registerClient(dto);
    }

    public User login(LoginUserDto dto) {
        Optional<Client> clientOpt = clientRepository.findByEmail(dto.getEmail());
        if (clientOpt.isPresent() && passwordEncoder.matches(dto.getPassword(), clientOpt.get().getPassword())) {
            return clientOpt.get();
        }

        Optional<Support> supportOpt = supportRepository.findByEmail(dto.getEmail());
        if (supportOpt.isPresent() && passwordEncoder.matches(dto.getPassword(), supportOpt.get().getPassword())) {
            return supportOpt.get();
        }

        throw new RuntimeException("Email o contraseña incorrectos");
    }
}
