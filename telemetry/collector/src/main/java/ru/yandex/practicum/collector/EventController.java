package ru.yandex.practicum.collector;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.SensorEvent.SensorEventProto;
import ru.yandex.practicum.telemetry.collector.service.handler.HubEventHandler;
import ru.yandex.practicum.collector.service.SensorEventService;
import ru.yandex.practicum.collector.mapper.SensorEventMapper;
import ru.yandex.practicum.collector.model.sensor.SensorEvent;
import ru.yandex.practicum.grpc.telemetry.event.HubEvent.HubEventProto;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
public class EventController extends CollectorControllerGrpc.CollectorControllerImplBase {

    private final SensorEventService sensorEventService;
    private final SensorEventMapper sensorEventMapper;
    private final Map<HubEventProto.PayloadCase, HubEventHandler> hubHandlers;

    public EventController(
            SensorEventService sensorEventService,
            SensorEventMapper sensorEventMapper,
            List<HubEventHandler> hubEventHandlers
    ) {
        this.sensorEventService = sensorEventService;
        this.sensorEventMapper = sensorEventMapper;
        this.hubHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(
                        HubEventHandler::getMessageType,
                        Function.identity()
                ));
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            SensorEvent event = sensorEventMapper.fromProto(request);
            sensorEventService.processEvent(event);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("❌ Ошибка при обработке SensorEventProto: {}", request, e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("📥 Получен HubEvent с типом: {}", request.getPayloadCase());

            HubEventHandler handler = hubHandlers.get(request.getPayloadCase());

            if (handler == null) {
                log.warn("⚠️ Нет обработчика для типа: {}", request.getPayloadCase());
                responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Нет обработчика для " + request.getPayloadCase())
                        .asRuntimeException());
                return;
            }

            handler.handle(request);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("❌ Ошибка при обработке HubEventProto: {}", request, e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}