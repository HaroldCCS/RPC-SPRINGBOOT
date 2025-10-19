package com.rpc.client.controller;

import com.rpc.client.grpc.FormGrpcClient;
import com.rpc.library.dto.FormRequestDto;
import com.rpc.library.dto.FormResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FormController {

    private final FormGrpcClient formGrpcClient;

    @PostMapping("/form")
    public ResponseEntity<FormResponseDto> submitForm(@RequestBody FormRequestDto formRequest) {
        log.info("Recibiendo solicitud REST para formulario: {}", formRequest);

        try {
            // Llamar al servidor gRPC a través del cliente
            FormResponseDto response = formGrpcClient.submitForm(formRequest);

            // Determinar el código de estado HTTP basado en la respuesta
            HttpStatus status = "OK".equals(response.getStatus()) ? HttpStatus.OK : HttpStatus.BAD_REQUEST;

            return ResponseEntity.status(status).body(response);

        } catch (Exception e) {
            log.error("Error procesando formulario", e);
            
            FormResponseDto errorResponse = FormResponseDto.builder()
                    .status("ERROR")
                    .message("Error procesando la solicitud: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Cliente gRPC funcionando correctamente");
    }
}

