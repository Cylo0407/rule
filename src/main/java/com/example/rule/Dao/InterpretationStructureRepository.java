package com.example.rule.Dao;

import com.example.rule.Model.PO.InterpretationStructureResPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterpretationStructureRepository extends JpaRepository<InterpretationStructureResPO, Integer> {
    List<InterpretationStructureResPO> findByDocId(String docId);
}
