package com.maleyk.flow_manager.dto;

import com.maleyk.flow_manager.model.RecordStatus;

import java.util.UUID;

public record FileStatusResponse(
        UUID id,
        RecordStatus recordStatus,
        String convertedPath
) {
}
