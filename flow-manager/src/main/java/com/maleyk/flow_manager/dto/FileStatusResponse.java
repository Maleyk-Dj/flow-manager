package com.maleyk.flow_manager.dto;

import com.maleyk.flow_manager.model.Status;

import java.util.UUID;

public record FileStatusResponse(
        UUID id,
        Status status,
        String convertedPath
) {
}
