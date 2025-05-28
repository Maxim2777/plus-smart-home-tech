package ru.yandex.practicum.telemetry.analyzer.service.grpc;

import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

@Component
@Slf4j
public class HubRouterClient {

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub stub;

    public void sendAction(DeviceActionRequest request) {
        try {
            stub.handleDeviceAction(request);
            log.info("📡 Команда отправлена: {}", request);
        } catch (StatusRuntimeException e) {
            log.error("❌ Ошибка отправки команды: {}", e.getMessage(), e);
        }
    }
}