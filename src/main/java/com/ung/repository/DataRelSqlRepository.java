package com.ung.repository;

import com.ung.entity.DataRel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SQL-based implementation for DATA_REL validation and querying
 * Provides parallel SQL query processing alongside QueryDSL
 */
@Component
@RequiredArgsConstructor
public class DataRelSqlRepository {
    
    private final EntityManager entityManager;
    
    /**
     * Validate DATA_REL using native SQL query
     * Parallel implementation to QueryDSL approach
     */
    @SuppressWarnings("unchecked")
    public List<DataRel> validateDataRel(String sourceId, String targetId, 
                                          String relType, String status) {
        StringBuilder sql = new StringBuilder(
            "SELECT dr.* FROM data_rel dr WHERE dr.is_valid = true"
        );
        
        List<String> conditions = new ArrayList<>();
        
        if (sourceId != null && !sourceId.isEmpty()) {
            conditions.add("dr.source_id = :sourceId");
        }
        
        if (targetId != null && !targetId.isEmpty()) {
            conditions.add("dr.target_id = :targetId");
        }
        
        if (relType != null && !relType.isEmpty()) {
            conditions.add("dr.rel_type = :relType");
        }
        
        if (status != null && !status.isEmpty()) {
            conditions.add("dr.status = :status");
        }
        
        if (!conditions.isEmpty()) {
            sql.append(" AND ").append(String.join(" AND ", conditions));
        }
        
        sql.append(" ORDER BY dr.priority DESC NULLS LAST, dr.created_at DESC");
        
        Query query = entityManager.createNativeQuery(sql.toString(), DataRel.class);
        
        if (sourceId != null && !sourceId.isEmpty()) {
            query.setParameter("sourceId", sourceId);
        }
        if (targetId != null && !targetId.isEmpty()) {
            query.setParameter("targetId", targetId);
        }
        if (relType != null && !relType.isEmpty()) {
            query.setParameter("relType", relType);
        }
        if (status != null && !status.isEmpty()) {
            query.setParameter("status", status);
        }
        
        return query.getResultList();
    }
    
    /**
     * Find DATA_REL by complex conditions using SQL
     * Parallel implementation to QueryDSL approach
     */
    @SuppressWarnings("unchecked")
    public List<DataRel> findByComplexConditions(String relType, Integer minPriority, 
                                                  LocalDateTime startDate) {
        StringBuilder sql = new StringBuilder(
            "SELECT dr.* FROM data_rel dr WHERE dr.is_valid = true"
        );
        
        List<String> conditions = new ArrayList<>();
        
        if (relType != null) {
            conditions.add("dr.rel_type = :relType");
        }
        
        if (minPriority != null) {
            conditions.add("dr.priority >= :minPriority");
        }
        
        if (startDate != null) {
            conditions.add("dr.created_at >= :startDate");
        }
        
        if (!conditions.isEmpty()) {
            sql.append(" AND ").append(String.join(" AND ", conditions));
        }
        
        sql.append(" ORDER BY dr.priority DESC NULLS LAST, dr.created_at DESC");
        
        Query query = entityManager.createNativeQuery(sql.toString(), DataRel.class);
        
        if (relType != null) {
            query.setParameter("relType", relType);
        }
        if (minPriority != null) {
            query.setParameter("minPriority", minPriority);
        }
        if (startDate != null) {
            query.setParameter("startDate", startDate);
        }
        
        return query.getResultList();
    }
    
    /**
     * Validate relationship consistency using SQL
     * Parallel implementation to QueryDSL approach
     */
    public boolean validateRelationshipConsistency(String sourceId, String targetId) {
        String sql = "SELECT COUNT(*) FROM data_rel dr " +
                    "WHERE dr.source_id = :sourceId " +
                    "AND dr.target_id = :targetId " +
                    "AND dr.is_valid = true " +
                    "AND dr.status = 'ACTIVE'";
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("sourceId", sourceId);
        query.setParameter("targetId", targetId);
        
        Number count = (Number) query.getSingleResult();
        return count.longValue() > 0;
    }
    
    /**
     * Find duplicate relationships using SQL
     * Parallel implementation to QueryDSL approach
     */
    @SuppressWarnings("unchecked")
    public List<DataRel> findDuplicateRelationships(String sourceId, String targetId, String relType) {
        String sql = "SELECT dr.* FROM data_rel dr " +
                    "WHERE dr.source_id = :sourceId " +
                    "AND dr.target_id = :targetId " +
                    "AND dr.rel_type = :relType " +
                    "AND dr.is_valid = true";
        
        Query query = entityManager.createNativeQuery(sql, DataRel.class);
        query.setParameter("sourceId", sourceId);
        query.setParameter("targetId", targetId);
        query.setParameter("relType", relType);
        
        return query.getResultList();
    }
}
