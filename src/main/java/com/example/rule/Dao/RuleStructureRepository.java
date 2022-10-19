package com.example.rule.Dao;

import com.example.rule.Model.PO.StructureResPO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RuleStructureRepository extends JpaRepository<StructureResPO, Integer> {
    StructureResPO save(StructureResPO policySplitPO);
}
