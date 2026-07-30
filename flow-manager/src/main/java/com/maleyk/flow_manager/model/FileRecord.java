package com.maleyk.flow_manager.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "file_records")
@Getter
@Setter
public class FileRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String originalFilename;

    private String sourcePath;

    private String convertedPath;

    @Enumerated(EnumType.STRING)
    private RecordStatus recordStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
