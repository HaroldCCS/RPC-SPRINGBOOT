package com.rpc.client.grpc;

import com.rpc.library.dto.FormRequestDto;
import com.rpc.library.dto.FormResponseDto;
import com.rpc.library.proto.FormRequest;
import com.rpc.library.proto.FormResponse;
import com.rpc.library.proto.FormServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FormGrpcClient {

    private final FormServiceGrpc.FormServiceBlockingStub blockingStub;

    public FormGrpcClient(GrpcChannelFactory channelFactory,
                          @Value("${grpc.client.form-service.authority}") String authority) {
        ManagedChannel channel = channelFactory.createChannel(authority);
        this.blockingStub = FormServiceGrpc.newBlockingStub(channel);
    }

    public FormResponseDto submitForm(FormRequestDto formDto) {
        log.info("Enviando formulario via gRPC: {}", formDto);

        try {
            // Convertir DTO a protobuf
            FormRequest request = FormRequest.newBuilder()
                    .setNombre(formDto.getNombre())
                    .setApellido(formDto.getApellido())
                    .setEdad(formDto.getEdad())
                    .setEmail(formDto.getEmail())
                    .build();

            // Llamar al servidor gRPC
            FormResponse response = blockingStub.submitForm(request);

            log.info("Respuesta recibida: status={}, message={}", response.getStatus(), response.getMessage());

            // Convertir respuesta protobuf a DTO
            return FormResponseDto.builder()
                    .status(response.getStatus())
                    .message(response.getMessage())
                    .timestamp(response.getTimestamp())
                    .build();

        } catch (StatusRuntimeException e) {
            log.error("Error en llamada gRPC: {}", e.getStatus());
            return FormResponseDto.builder()
                    .status("ERROR")
                    .message("Error de comunicación con el servidor: " + e.getStatus().getDescription())
                    .timestamp(System.currentTimeMillis())
                    .build();
        } catch (Exception e) {
            log.error("Error inesperado", e);
            return FormResponseDto.builder()
                    .status("ERROR")
                    .message("Error inesperado: " + e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }
}

