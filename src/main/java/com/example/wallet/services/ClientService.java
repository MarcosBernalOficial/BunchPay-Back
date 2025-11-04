package com.example.wallet.services;

import com.example.wallet.controllers.exceptions.*;
import com.example.wallet.dtos.*;
import com.example.wallet.model.enums.Role;
import com.example.wallet.model.implementations.AccountClient;
import com.example.wallet.model.implementations.Client;
import com.example.wallet.repository.ClientRepository;
import com.example.wallet.repository.SupportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AccountClientService accountClientService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private SupportRepository supportRepository;

    public Client getByEmail(String email) {
        return clientRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedAccessException("Cliente no encontrado con email: " + email));
    }

    // Registro
    public AccountClient registerClient(RegisterUserDto request) {

        // Verificar email duplicado en clientes
        if (clientRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("El correo ya está registrado.");
        }

        // Verificar email duplicado también en la tabla de soportes/admins
        if (supportRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("El correo ya está registrado.");
        }

        if (clientRepository.findByDni(request.getDni()).isPresent()) {
            throw new DniAlreadyExistException("El dni ya está registrado.");
        }

        Client client = new Client();
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setEmail(request.getEmail());
        client.setDni(request.getDni());
        client.setPassword(passwordEncoder.encode(request.getPassword()));
        client.setRole(Role.CLIENT);
        Client savedClient = clientRepository.save(client);
        return accountClientService.createAccountClient(savedClient);
    }

    // Login
    public Client login(LoginUserDto request) {
        Client client = clientRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidLoginException("Correo no registrado."));

        if (!passwordEncoder.matches(request.getPassword(), client.getPassword())) {
            throw new InvalidLoginException("Contraseña incorrecta.");
        }

        return client;
    }

    // ViewProfile
    public ClientProfileDto getProfile(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedAccessException("Cliente no encontrado con email: " + email));

        ClientProfileDto dto = new ClientProfileDto();
        dto.setFirstName(client.getFirstName());
        dto.setLastName(client.getLastName());
        dto.setEmail(client.getEmail());
        dto.setDni(client.getDni());

        return dto;
    }

    // UpdateProfile
    public void updateProfile(String email, UserUpdateDto dto) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedAccessException("Cliente no encontrado. Inicie sesion."));

        client.setFirstName(dto.getFirstName());
        client.setLastName(dto.getLastName());

        clientRepository.save(client);
    }

    // ChangePassword
    public void changePassword(String email, PasswordChangeDto dto) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedAccessException("Cliente no encontrado. Inicie sesion."));
        if (!passwordEncoder.matches(dto.getCurrentPassword(), client.getPassword())) {
            throw new InvalidLoginException("La contraseña actual no es correcta.");
        }
        if (passwordEncoder.matches(dto.getNewPassword(), client.getPassword())) {
            throw new RepeatedPasswordException("La nueva contraseña no puede ser igual a la actual.");
        }
        String newEncodedPassword = passwordEncoder.encode(dto.getNewPassword());
        client.setPassword(newEncodedPassword);
        clientRepository.save(client);
    }
}
