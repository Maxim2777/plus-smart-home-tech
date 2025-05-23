package ru.yandex.practicum.aggregator.exception;

public class DeserializationException extends RuntimeException {
    public DeserializationException(String message, Throwable cause) {
        super(message, cause);
    }
}