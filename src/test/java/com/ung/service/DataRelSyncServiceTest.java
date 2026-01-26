package com.ung.service;

import com.ung.entity.DataRel;
import com.ung.repository.DataRelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for DataRelSyncService
 * Tests both QueryDSL and SQL implementations in parallel
 */
@SpringBootTest
@Transactional
class DataRelSyncServiceTest {
    
    @Autowired
    private DataRelSyncService syncService;
    
    @Autowired
    private DataRelRepository repository;
    
    @BeforeEach
    void setUp() {
        repository.deleteAll();
        
        // Create test data
        List<DataRel> testData = Arrays.asList(
            DataRel.builder()
                .sourceId("SRC001")
                .targetId("TGT001")
                .relType("PARENT_CHILD")
                .status("ACTIVE")
                .priority(10)
                .metadata("{\"key\": \"value1\"}")
                .isValid(true)
                .build(),
            DataRel.builder()
                .sourceId("SRC001")
                .targetId("TGT002")
                .relType("PARENT_CHILD")
                .status("ACTIVE")
                .priority(5)
                .metadata("{\"key\": \"value2\"}")
                .isValid(true)
                .build(),
            DataRel.builder()
                .sourceId("SRC002")
                .targetId("TGT003")
                .relType("SIBLING")
                .status("INACTIVE")
                .priority(3)
                .metadata("{\"key\": \"value3\"}")
                .isValid(true)
                .build(),
            DataRel.builder()
                .sourceId("SRC003")
                .targetId("TGT004")
                .relType("PARENT_CHILD")
                .status("ACTIVE")
                .priority(8)
                .metadata("{\"key\": \"value4\"}")
                .isValid(false)
                .build()
        );
        
        repository.saveAll(testData);
    }
    
    @Test
    void testValidateDataRelSync_withAllParameters() {
        // When
        DataRelSyncService.ValidationResult result = syncService.validateDataRelSync(
            "SRC001", "TGT001", "PARENT_CHILD", "ACTIVE"
        );
        
        // Then
        assertNotNull(result);
        assertTrue(result.isConsistent(), "QueryDSL and SQL results should be consistent");
        assertEquals(1, result.getQueryDslResults().size());
        assertEquals(1, result.getSqlResults().size());
        
        DataRel queryDslResult = result.getQueryDslResults().get(0);
        DataRel sqlResult = result.getSqlResults().get(0);
        
        assertEquals(queryDslResult.getId(), sqlResult.getId());
        assertEquals("SRC001", queryDslResult.getSourceId());
        assertEquals("TGT001", queryDslResult.getTargetId());
    }
    
    @Test
    void testValidateDataRelSync_withSourceIdOnly() {
        // When
        DataRelSyncService.ValidationResult result = syncService.validateDataRelSync(
            "SRC001", null, null, null
        );
        
        // Then
        assertTrue(result.isConsistent());
        assertEquals(2, result.getQueryDslResults().size());
        assertEquals(2, result.getSqlResults().size());
    }
    
    @Test
    void testValidateDataRelSync_withRelTypeOnly() {
        // When
        DataRelSyncService.ValidationResult result = syncService.validateDataRelSync(
            null, null, "PARENT_CHILD", null
        );
        
        // Then
        assertTrue(result.isConsistent());
        // Should find 2 valid PARENT_CHILD relationships (SRC003->TGT004 is invalid)
        assertEquals(2, result.getQueryDslResults().size());
        assertEquals(2, result.getSqlResults().size());
    }
    
    @Test
    void testValidateComplexConditions() {
        // When
        DataRelSyncService.ValidationResult result = syncService.validateComplexConditions(
            "PARENT_CHILD", 5, null
        );
        
        // Then
        assertTrue(result.isConsistent());
        // Should find PARENT_CHILD with priority >= 5 (priority 10 and 5, excluding invalid)
        assertEquals(2, result.getQueryDslResults().size());
        assertEquals(2, result.getSqlResults().size());
    }
    
    @Test
    void testValidateComplexConditions_withDateFilter() {
        // When
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        DataRelSyncService.ValidationResult result = syncService.validateComplexConditions(
            "PARENT_CHILD", null, yesterday
        );
        
        // Then
        assertTrue(result.isConsistent());
        // All valid PARENT_CHILD created after yesterday
        assertEquals(2, result.getQueryDslResults().size());
        assertEquals(2, result.getSqlResults().size());
    }
    
    @Test
    void testCheckRelationshipConsistency_exists() {
        // When
        DataRelSyncService.ConsistencyCheckResult result = 
            syncService.checkRelationshipConsistency("SRC001", "TGT001");
        
        // Then
        assertTrue(result.isConsistent());
        assertTrue(result.isQueryDslValid());
        assertTrue(result.isSqlValid());
    }
    
    @Test
    void testCheckRelationshipConsistency_notExists() {
        // When
        DataRelSyncService.ConsistencyCheckResult result = 
            syncService.checkRelationshipConsistency("NONEXISTENT", "NONE");
        
        // Then
        assertTrue(result.isConsistent());
        assertFalse(result.isQueryDslValid());
        assertFalse(result.isSqlValid());
    }
    
    @Test
    void testFindDuplicatesSync_noDuplicates() {
        // When
        DataRelSyncService.ValidationResult result = syncService.findDuplicatesSync(
            "SRC001", "TGT001", "PARENT_CHILD"
        );
        
        // Then
        assertTrue(result.isConsistent());
        assertEquals(1, result.getQueryDslResults().size());
        assertEquals(1, result.getSqlResults().size());
    }
    
    @Test
    void testFindDuplicatesSync_withDuplicates() {
        // Create duplicate
        DataRel duplicate = DataRel.builder()
            .sourceId("SRC001")
            .targetId("TGT001")
            .relType("PARENT_CHILD")
            .status("ACTIVE")
            .priority(15)
            .isValid(true)
            .build();
        repository.save(duplicate);
        
        // When
        DataRelSyncService.ValidationResult result = syncService.findDuplicatesSync(
            "SRC001", "TGT001", "PARENT_CHILD"
        );
        
        // Then
        assertTrue(result.isConsistent());
        assertEquals(2, result.getQueryDslResults().size());
        assertEquals(2, result.getSqlResults().size());
    }
    
    @Test
    void testSaveDataRel() {
        // Given
        DataRel newRel = DataRel.builder()
            .sourceId("SRC_NEW")
            .targetId("TGT_NEW")
            .relType("NEW_TYPE")
            .status("PENDING")
            .priority(1)
            .isValid(true)
            .build();
        
        // When
        DataRel saved = syncService.saveDataRel(newRel);
        
        // Then
        assertNotNull(saved.getId());
        assertEquals("SRC_NEW", saved.getSourceId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
    }
    
    @Test
    void testPriorityOrdering() {
        // When - search by relType only
        DataRelSyncService.ValidationResult result = syncService.validateDataRelSync(
            null, null, "PARENT_CHILD", null
        );
        
        // Then - should be ordered by priority DESC
        assertTrue(result.isConsistent());
        List<DataRel> queryDslResults = result.getQueryDslResults();
        List<DataRel> sqlResults = result.getSqlResults();
        
        assertEquals(2, queryDslResults.size());
        // Priority 10 should come before priority 5
        assertTrue(queryDslResults.get(0).getPriority() >= queryDslResults.get(1).getPriority());
        assertTrue(sqlResults.get(0).getPriority() >= sqlResults.get(1).getPriority());
    }
}
