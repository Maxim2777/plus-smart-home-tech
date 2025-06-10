package ru.yandex.practicum.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.practicum.collector.service.HubEventService;
import ru.yandex.practicum.collector.service.SensorEventService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc.CollectorControllerImplBase;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

@GrpcService
public class EventController extends CollectorControllerImplBase {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final SensorEventService sensorEventService;
    private final HubEventService hubEventService;

    public EventController(SensorEventService sensorEventService, HubEventService hubEventService) {
        this.sensorEventService = sensorEventService;
        this.hubEventService = hubEventService;
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        String hubId = request.getHubId();
        String sensorId = request.getId();
        log.info("📡 Получено SensorEvent: hubId={}, sensorId={}, timestamp={}", hubId, sensorId, request.getTimestamp());

        try {
            sensorEventService.handleSensorEvent(request);
            log.info("✅ SensorEvent обработан: hubId={}, sensorId={}", hubId, sensorId);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("❌ Ошибка обработки SensorEvent (hubId={}, sensorId={}): {}", hubId, sensorId, e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL.withDescription(e.getMessage()).withCause(e)
            ));
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        String hubId = request.getHubId();
        log.info("📡 Получено HubEvent: hubId={}, timestamp={}", hubId, request.getTimestamp());

        try {
            hubEventService.handleHubEvent(request);
            log.info("✅ HubEvent обработан: hubId={}", hubId);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("❌ Ошибка обработки HubEvent (hubId={}): {}", hubId, e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL.withDescription(e.getMessage()).withCause(e)
            ));
        }
    }
}