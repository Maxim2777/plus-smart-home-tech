package ru.yandex.practicum.telemetry.analyzer.config;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

@Configuration
public class GrpcClientConfig {

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterStub;

    @Bean
    public HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterStub() {
        return hubRouterStub;
    }
}