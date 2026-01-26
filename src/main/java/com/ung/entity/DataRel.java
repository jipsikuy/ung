package com.ung.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing DATA_REL (Data Relationship)
 * Core entity for data synchronization and validation
 */
@Entity
@Table(name = "data_rel")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataRel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "source_id", nullable = false)
    private String sourceId;
    
    @Column(name = "target_id", nullable = false)
    private String targetId;
    
    @Column(name = "rel_type", nullable = false)
    private String relType;
    
    @Column(name = "status", nullable = false)
    private String status;
    
    @Column(name = "priority")
    private Integer priority;
    
    @Column(name = "metadata")
    private String metadata;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "is_valid", nullable = false)
    private Boolean isValid;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isValid == null) {
            isValid = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
