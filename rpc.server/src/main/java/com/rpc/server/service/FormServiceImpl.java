package com.rpc.server.service;

import com.rpc.library.proto.FormRequest;
import com.rpc.library.proto.FormResponse;
import com.rpc.library.proto.FormServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@GrpcService
public class FormServiceImpl extends FormServiceGrpc.FormServiceImplBase {

    // Simulación de almacenamiento en memoria
    private final ConcurrentHashMap<Long, FormRequest> formStorage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public void submitForm(FormRequest request, StreamObserver<FormResponse> responseObserver) {
        log.info("Recibiendo formulario: nombre={}, apellido={}, edad={}, email={}", 
                request.getNombre(), request.getApellido(), request.getEdad(), request.getEmail());

        try {
            // Validaciones básicas
            if (request.getNombre() == null || request.getNombre().trim().isEmpty()) {
                sendErrorResponse(responseObserver, "El nombre es requerido");
                return;
            }

            if (request.getApellido() == null || request.getApellido().trim().isEmpty()) {
                sendErrorResponse(responseObserver, "El apellido es requerido");
                return;
            }

            if (request.getEdad() <= 0 || request.getEdad() > 150) {
                sendErrorResponse(responseObserver, "La edad debe estar entre 1 y 150");
                return;
            }

            if (request.getEmail() == null || !request.getEmail().contains("@")) {
                sendErrorResponse(responseObserver, "El email no es válido");
                return;
            }

            // Simular guardado del formulario
            long id = idGenerator.getAndIncrement();
            formStorage.put(id, request);

            log.info("Formulario guardado exitosamente con ID: {}", id);

            // Crear respuesta exitosa
            FormResponse response = FormResponse.newBuilder()
                    .setStatus("OK")
                    .setMessage("Formulario recibido y procesado exitosamente. ID: " + id)
                    .setTimestamp(Instant.now().toEpochMilli())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error procesando formulario", e);
            sendErrorResponse(responseObserver, "Error interno del servidor: " + e.getMessage());
        }
    }

    private void sendErrorResponse(StreamObserver<FormResponse> responseObserver, String errorMessage) {
        log.warn("Enviando respuesta de error: {}", errorMessage);
        
        FormResponse response = FormResponse.newBuilder()
                .setStatus("ERROR")
                .setMessage(errorMessage)
                .setTimestamp(Instant.now().toEpochMilli())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}

