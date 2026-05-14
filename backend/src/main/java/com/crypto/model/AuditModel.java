package com.crypto.model;

import lombok.Data;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
@Data
public abstract class AuditModel {
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
