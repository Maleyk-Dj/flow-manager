package com.maleyk.flow_manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class
FileConversionResult {
    String originalMessageId;
    ConversionStatus status;
    String bucket;
    String pdfPath;
    String errorMessage;

    public static FileConversionResult success(String messageId, String bucket, String pdfPath) {
        return new FileConversionResult(messageId, ConversionStatus.SUCCESS, bucket, pdfPath, null);
    }

    public static FileConversionResult failed(String messageId, String errorMessage) {
        return new FileConversionResult(messageId, ConversionStatus.FAILED, null, null, errorMessage);
    }
}
