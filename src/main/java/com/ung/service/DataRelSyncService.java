package com.ung.service;

import com.ung.entity.DataRel;
import com.ung.repository.DataRelQueryDslRepository;
import com.ung.repository.DataRelRepository;
import com.ung.repository.DataRelSqlRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Data Synchronization Service
 * Manages parallel execution of QueryDSL and SQL implementations
 * Provides synchronized validation structure
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataRelSyncService {
    
    private final DataRelRepository dataRelRepository;
    private final DataRelQueryDslRepository queryDslRepository;
    private final DataRelSqlRepository sqlRepository;
    
    /**
     * Synchronized validation using both QueryDSL and SQL
     * Ensures consistency between both implementations
     */
    @Transactional(readOnly = true)
    public ValidationResult validateDataRelSync(String sourceId, String targetId, 
                                                 String relType, String status) {
        log.info("Starting synchronized validation for sourceId={}, targetId={}", sourceId, targetId);
        
        // Execute QueryDSL validation
        List<DataRel> queryDslResults = queryDslRepository.validateDataRel(
            sourceId, targetId, relType, status
        );
        
        // Execute SQL validation in parallel
        List<DataRel> sqlResults = sqlRepository.validateDataRel(
            sourceId, targetId, relType, status
        );
        
        // Compare results for consistency
        boolean isConsistent = compareResults(queryDslResults, sqlResults);
        
        log.info("Validation completed. QueryDSL count={}, SQL count={}, consistent={}", 
                queryDslResults.size(), sqlResults.size(), isConsistent);
        
        return ValidationResult.builder()
                .queryDslResults(queryDslResults)
                .sqlResults(sqlResults)
                .isConsistent(isConsistent)
                .validationTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * Validate complex conditions using both implementations
     */
    @Transactional(readOnly = true)
    public ValidationResult validateComplexConditions(String relType, Integer minPriority, 
                                                      LocalDateTime startDate) {
        log.info("Starting complex condition validation for relType={}", relType);
        
        // QueryDSL approach
        List<DataRel> queryDslResults = queryDslRepository.findByComplexConditions(
            relType, minPriority, startDate
        );
        
        // SQL approach
        List<DataRel> sqlResults = sqlRepository.findByComplexConditions(
            relType, minPriority, startDate
        );
        
        boolean isConsistent = compareResults(queryDslResults, sqlResults);
        
        log.info("Complex validation completed. Consistent={}", isConsistent);
        
        return ValidationResult.builder()
                .queryDslResults(queryDslResults)
                .sqlResults(sqlResults)
                .isConsistent(isConsistent)
                .validationTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * Validate relationship consistency using both methods
     */
    @Transactional(readOnly = true)
    public ConsistencyCheckResult checkRelationshipConsistency(String sourceId, String targetId) {
        log.info("Checking relationship consistency for sourceId={}, targetId={}", sourceId, targetId);
        
        // QueryDSL check
        boolean queryDslValid = queryDslRepository.validateRelationshipConsistency(
            sourceId, targetId
        );
        
        // SQL check
        boolean sqlValid = sqlRepository.validateRelationshipConsistency(
            sourceId, targetId
        );
        
        boolean isConsistent = queryDslValid == sqlValid;
        
        log.info("Consistency check completed. QueryDSL={}, SQL={}, consistent={}", 
                queryDslValid, sqlValid, isConsistent);
        
        return ConsistencyCheckResult.builder()
                .queryDslValid(queryDslValid)
                .sqlValid(sqlValid)
                .isConsistent(isConsistent)
                .checkTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * Find duplicates using both implementations and compare
     */
    @Transactional(readOnly = true)
    public ValidationResult findDuplicatesSync(String sourceId, String targetId, String relType) {
        log.info("Finding duplicates for sourceId={}, targetId={}, relType={}", 
                sourceId, targetId, relType);
        
        List<DataRel> queryDslDuplicates = queryDslRepository.findDuplicateRelationships(
            sourceId, targetId, relType
        );
        
        List<DataRel> sqlDuplicates = sqlRepository.findDuplicateRelationships(
            sourceId, targetId, relType
        );
        
        boolean isConsistent = compareResults(queryDslDuplicates, sqlDuplicates);
        
        log.info("Duplicate search completed. Count={}, consistent={}", 
                queryDslDuplicates.size(), isConsistent);
        
        return ValidationResult.builder()
                .queryDslResults(queryDslDuplicates)
                .sqlResults(sqlDuplicates)
                .isConsistent(isConsistent)
                .validationTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * Save DataRel entity
     */
    @Transactional
    public DataRel saveDataRel(DataRel dataRel) {
        log.info("Saving DataRel: sourceId={}, targetId={}", 
                dataRel.getSourceId(), dataRel.getTargetId());
        return dataRelRepository.save(dataRel);
    }
    
    /**
     * Compare results from QueryDSL and SQL implementations
     */
    private boolean compareResults(List<DataRel> queryDslResults, List<DataRel> sqlResults) {
        if (queryDslResults.size() != sqlResults.size()) {
            log.warn("Result count mismatch: QueryDSL={}, SQL={}", 
                    queryDslResults.size(), sqlResults.size());
            return false;
        }
        
        // Compare IDs (assuming both are ordered the same way)
        for (int i = 0; i < queryDslResults.size(); i++) {
            if (!Objects.equals(queryDslResults.get(i).getId(), sqlResults.get(i).getId())) {
                log.warn("Result order/content mismatch at index {}", i);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Validation result DTO
     */
    @Data
    @Builder
    public static class ValidationResult {
        private List<DataRel> queryDslResults;
        private List<DataRel> sqlResults;
        private boolean isConsistent;
        private LocalDateTime validationTime;
    }
    
    /**
     * Consistency check result DTO
     */
    @Data
    @Builder
    public static class ConsistencyCheckResult {
        private boolean queryDslValid;
        private boolean sqlValid;
        private boolean isConsistent;
        private LocalDateTime checkTime;
    }
}
