package com.ung.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ung.entity.DataRel;
import com.ung.entity.QDataRel;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * QueryDSL-based implementation for DATA_REL validation and querying
 * Provides dynamic query processing with QueryDSL syntax
 */
@Component
@RequiredArgsConstructor
public class DataRelQueryDslRepository {
    
    private final EntityManager entityManager;
    
    /**
     * Validate DATA_REL using QueryDSL dynamic query
     */
    public List<DataRel> validateDataRel(String sourceId, String targetId, 
                                          String relType, String status) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        QDataRel qDataRel = QDataRel.dataRel;
        
        BooleanBuilder builder = new BooleanBuilder();
        
        // Dynamic condition building
        if (sourceId != null && !sourceId.isEmpty()) {
            builder.and(qDataRel.sourceId.eq(sourceId));
        }
        
        if (targetId != null && !targetId.isEmpty()) {
            builder.and(qDataRel.targetId.eq(targetId));
        }
        
        if (relType != null && !relType.isEmpty()) {
            builder.and(qDataRel.relType.eq(relType));
        }
        
        if (status != null && !status.isEmpty()) {
            builder.and(qDataRel.status.eq(status));
        }
        
        // Add validation condition
        builder.and(qDataRel.isValid.isTrue());
        
        return queryFactory
                .selectFrom(qDataRel)
                .where(builder)
                .orderBy(qDataRel.priority.desc().nullsLast(), qDataRel.createdAt.desc())
                .fetch();
    }
    
    /**
     * Find DATA_REL by complex conditions using QueryDSL
     */
    public List<DataRel> findByComplexConditions(String relType, Integer minPriority, 
                                                  LocalDateTime startDate) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        QDataRel qDataRel = QDataRel.dataRel;
        
        BooleanBuilder builder = new BooleanBuilder();
        
        if (relType != null) {
            builder.and(qDataRel.relType.eq(relType));
        }
        
        if (minPriority != null) {
            builder.and(qDataRel.priority.goe(minPriority));
        }
        
        if (startDate != null) {
            builder.and(qDataRel.createdAt.goe(startDate));
        }
        
        builder.and(qDataRel.isValid.isTrue());
        
        return queryFactory
                .selectFrom(qDataRel)
                .where(builder)
                .orderBy(qDataRel.priority.desc().nullsLast(), qDataRel.createdAt.desc())
                .fetch();
    }
    
    /**
     * Validate relationship consistency using QueryDSL
     */
    public boolean validateRelationshipConsistency(String sourceId, String targetId) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        QDataRel qDataRel = QDataRel.dataRel;
        
        BooleanExpression expression = qDataRel.sourceId.eq(sourceId)
                .and(qDataRel.targetId.eq(targetId))
                .and(qDataRel.isValid.isTrue())
                .and(qDataRel.status.eq("ACTIVE"));
        
        Long count = queryFactory
                .select(qDataRel.count())
                .from(qDataRel)
                .where(expression)
                .fetchOne();
        
        return count != null && count > 0;
    }
    
    /**
     * Find duplicate relationships using QueryDSL
     */
    public List<DataRel> findDuplicateRelationships(String sourceId, String targetId, String relType) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        QDataRel qDataRel = QDataRel.dataRel;
        
        return queryFactory
                .selectFrom(qDataRel)
                .where(qDataRel.sourceId.eq(sourceId)
                        .and(qDataRel.targetId.eq(targetId))
                        .and(qDataRel.relType.eq(relType))
                        .and(qDataRel.isValid.isTrue()))
                .fetch();
    }
}
