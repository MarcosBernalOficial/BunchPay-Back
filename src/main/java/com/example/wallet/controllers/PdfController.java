package com.example.wallet.controllers;

import com.example.wallet.dtos.PdfRequestDto;
import com.example.wallet.services.EmailService;
import com.example.wallet.services.PdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pdf")
@Tag(name = "Pdf", description = "Gestor de comprbantes pdf")
public class PdfController {

    @Autowired
    private PdfService pdfService;

    @Autowired
    private EmailService emailService;

    @Operation(summary = "Generar pdf", description = "Genera un pdf como comprobante")
    @PostMapping("/generate")
    public ResponseEntity<byte[]> generatePdf(@RequestBody PdfRequestDto request) {
        try {
            String html = request.getHtml();

            byte[] pdfBytes = pdfService.generatePdfFromHtml(html);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "comprobante.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "Enviar pdf", description = "Envia un pdf por correo")
    @PostMapping("/send-pdf-email")
    public ResponseEntity<String> sendPdfEmail(@Valid @RequestBody PdfRequestDto request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body("Datos inválidos en la solicitud");
        }
        try {
            byte[] pdfBytes = pdfService.generatePdfFromHtml(request.getHtml());
            emailService.sendPdfByEmail(request.getEmail(), pdfBytes);
            return ResponseEntity.ok("PDF enviado por email");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al enviar el email");
        }
    }

}
