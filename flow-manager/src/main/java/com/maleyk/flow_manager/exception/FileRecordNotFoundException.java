package com.maleyk.flow_manager.exception;

import java.util.UUID;

public class FileRecordNotFoundException extends RuntimeException {
    public FileRecordNotFoundException(String message) {
        super(message);
    }

    public FileRecordNotFoundException(UUID id) {
        super("Запись не найдена: " + id);
    }
}
