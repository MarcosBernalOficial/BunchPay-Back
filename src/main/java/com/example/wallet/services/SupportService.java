package com.example.wallet.services;

import com.example.wallet.controllers.exceptions.UnauthorizedAccessException;
import com.example.wallet.dtos.SupportProfileDto;
import com.example.wallet.model.implementations.Support;
import com.example.wallet.repository.SupportRepository;
import com.example.wallet.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.wallet.controllers.exceptions.EmailAlreadyExistsException;

import java.util.List;

@Service
public class SupportService {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private SupportRepository supportRepository;

    @Autowired
    private ClientRepository clientRepository;

    public Support getByEmail(String email) {
        return supportRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedAccessException("Soporte no encontrado con email: " + email));
    }

    public List<Support> getAllSupports() {
        return supportRepository.findAll(); // usado por Admin para listar soporte
    }

    public Support save(Support support) {
        // Verificar email duplicado en clientes también
        if (clientRepository.findByEmail(support.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("El correo ya está registrado.");
        }

        // Verificar email duplicado en soporte
        if (supportRepository.findByEmail(support.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("El correo ya está registrado.");
        }

        String encryptedPassword = passwordEncoder.encode(support.getPassword());
        support.setPassword(encryptedPassword);

        return supportRepository.save(support);
    }

    public Support updateSupport(Long id, SupportProfileDto dto) {
        Support support = supportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Soporte no encontrado."));

        support.setFirstName(dto.getFirstName());
        support.setLastName(dto.getLastName());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            support.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return supportRepository.save(support);
    }

    public void deleteSupportById(Long id) {
        if (!supportRepository.existsById(id)) {
            throw new RuntimeException("Soporte con ID " + id + " no encontrado.");
        }
        supportRepository.deleteById(id);
    }
}
