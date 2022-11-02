package com.example.rule.Dao;

import com.example.rule.Model.PO.PenaltyCaseStructureResPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PenaltyCaseStructureRepository extends JpaRepository<PenaltyCaseStructureResPO, Integer> {
    List<PenaltyCaseStructureResPO> findByDocId(String docId);
}
