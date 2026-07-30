package com.maleyk.flow_manager.dto;

import lombok.Data;

@Data
public class FileConversionRequest {
    private String messageId;
    private String filePath;
    private String bucket;
}
