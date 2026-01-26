package com.ung.repository;

import com.ung.entity.DataRel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository for DataRel with QueryDSL support
 */
@Repository
public interface DataRelRepository extends JpaRepository<DataRel, Long>, 
                                          QuerydslPredicateExecutor<DataRel> {
    
    List<DataRel> findBySourceId(String sourceId);
    
    List<DataRel> findByTargetId(String targetId);
    
    List<DataRel> findByRelType(String relType);
    
    List<DataRel> findByStatus(String status);
}
