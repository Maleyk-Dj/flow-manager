package com.maleyk.flow_manager.dto;

public record FileDownload(
        byte[] content,
        String fileName
) {
}
