package io.jmlim.springboot.sqs.exception;

import lombok.Getter;

public class CsvFileWriterException extends RuntimeException {
    @Getter
    private String message;

    public CsvFileWriterException(String message) {
        super(message);

        this.message = message;
    }
}