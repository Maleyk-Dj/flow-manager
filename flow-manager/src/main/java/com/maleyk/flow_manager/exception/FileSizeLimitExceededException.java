package com.maleyk.flow_manager.exception;

public class FileSizeLimitExceededException extends RuntimeException {
  public FileSizeLimitExceededException(String message) {
    super(message);
  }
}
