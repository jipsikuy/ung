package com.ung.demo;

import com.ung.entity.DataRel;
import com.ung.service.DataRelSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Demo application showing QueryDSL and SQL synchronization
 * Uncomment @Component to enable demo on startup
 */
// @Component
@RequiredArgsConstructor
@Slf4j
public class DataRelSyncDemo implements CommandLineRunner {
    
    private final DataRelSyncService syncService;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("========================================");
        log.info("DATA_REL Synchronization Demo");
        log.info("========================================\n");
        
        // Create sample data
        createSampleData();
        
        // Demo 1: Validate with all parameters
        demo1_validateWithAllParameters();
        
        // Demo 2: Validate with complex conditions
        demo2_validateComplexConditions();
        
        // Demo 3: Check relationship consistency
        demo3_checkConsistency();
        
        // Demo 4: Find duplicates
        demo4_findDuplicates();
        
        log.info("\n========================================");
        log.info("Demo Complete!");
        log.info("========================================");
    }
    
    private void createSampleData() {
        log.info("Creating sample data...");
        
        DataRel rel1 = DataRel.builder()
                .sourceId("USER001")
                .targetId("PROJECT001")
                .relType("MEMBER_OF")
                .status("ACTIVE")
                .priority(10)
                .metadata("{\"role\": \"admin\"}")
                .isValid(true)
                .build();
        syncService.saveDataRel(rel1);
        
        DataRel rel2 = DataRel.builder()
                .sourceId("USER002")
                .targetId("PROJECT001")
                .relType("MEMBER_OF")
                .status("ACTIVE")
                .priority(5)
                .metadata("{\"role\": \"developer\"}")
                .isValid(true)
                .build();
        syncService.saveDataRel(rel2);
        
        DataRel rel3 = DataRel.builder()
                .sourceId("PROJECT001")
                .targetId("DEPARTMENT001")
                .relType("BELONGS_TO")
                .status("ACTIVE")
                .priority(8)
                .metadata("{\"department\": \"engineering\"}")
                .isValid(true)
                .build();
        syncService.saveDataRel(rel3);
        
        log.info("Sample data created successfully!\n");
    }
    
    private void demo1_validateWithAllParameters() {
        log.info("DEMO 1: Validate with specific parameters");
        log.info("-------------------------------------------");
        
        DataRelSyncService.ValidationResult result = syncService.validateDataRelSync(
                "USER001", "PROJECT001", "MEMBER_OF", "ACTIVE"
        );
        
        log.info("QueryDSL Results: {} records", result.getQueryDslResults().size());
        log.info("SQL Results: {} records", result.getSqlResults().size());
        log.info("Consistent: {}", result.isConsistent());
        
        if (!result.getQueryDslResults().isEmpty()) {
            DataRel rel = result.getQueryDslResults().get(0);
            log.info("Sample record: {} -> {} ({})", 
                    rel.getSourceId(), rel.getTargetId(), rel.getRelType());
        }
        log.info("");
    }
    
    private void demo2_validateComplexConditions() {
        log.info("DEMO 2: Validate with complex conditions");
        log.info("-------------------------------------------");
        
        DataRelSyncService.ValidationResult result = syncService.validateComplexConditions(
                "MEMBER_OF", 5, LocalDateTime.now().minusDays(1)
        );
        
        log.info("QueryDSL Results: {} records", result.getQueryDslResults().size());
        log.info("SQL Results: {} records", result.getSqlResults().size());
        log.info("Consistent: {}", result.isConsistent());
        log.info("Found records with relType='MEMBER_OF' and priority >= 5");
        log.info("");
    }
    
    private void demo3_checkConsistency() {
        log.info("DEMO 3: Check relationship consistency");
        log.info("-------------------------------------------");
        
        DataRelSyncService.ConsistencyCheckResult result1 = 
                syncService.checkRelationshipConsistency("USER001", "PROJECT001");
        
        log.info("Checking USER001 -> PROJECT001:");
        log.info("  QueryDSL Valid: {}", result1.isQueryDslValid());
        log.info("  SQL Valid: {}", result1.isSqlValid());
        log.info("  Consistent: {}", result1.isConsistent());
        
        DataRelSyncService.ConsistencyCheckResult result2 = 
                syncService.checkRelationshipConsistency("NONEXISTENT", "NONE");
        
        log.info("Checking NONEXISTENT -> NONE:");
        log.info("  QueryDSL Valid: {}", result2.isQueryDslValid());
        log.info("  SQL Valid: {}", result2.isSqlValid());
        log.info("  Consistent: {}", result2.isConsistent());
        log.info("");
    }
    
    private void demo4_findDuplicates() {
        log.info("DEMO 4: Find duplicate relationships");
        log.info("-------------------------------------------");
        
        DataRelSyncService.ValidationResult result = syncService.findDuplicatesSync(
                "USER001", "PROJECT001", "MEMBER_OF"
        );
        
        log.info("QueryDSL Results: {} duplicates", result.getQueryDslResults().size());
        log.info("SQL Results: {} duplicates", result.getSqlResults().size());
        log.info("Consistent: {}", result.isConsistent());
        log.info("");
    }
}
