package ru.yandex.practicum.telemetry.analyzer.service.grpc;

import com.google.protobuf.Empty;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubRouterClient {

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub stub;

    public void sendAction(DeviceActionRequest request) {
        try {
            stub.handleDeviceAction(request);
            log.info("📡 Команда отправлена: sensor={}, action={}, value={}",
                    request.getAction().getSensorId(),
                    request.getAction().getType(),
                    request.getAction().getValue());
        } catch (StatusRuntimeException e) {
            log.error("❌ Ошибка отправки команды в HubRouter: {}", e.getMessage(), e);
        }
    }
}

