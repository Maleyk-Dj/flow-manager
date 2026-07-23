package com.maleyk.flow_manager.repository;

import com.maleyk.flow_manager.model.FileRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileRecordRepository extends JpaRepository<FileRecord, UUID> {
}
