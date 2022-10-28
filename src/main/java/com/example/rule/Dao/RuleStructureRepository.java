package com.example.rule.Dao;

import com.example.rule.Model.PO.RuleStructureResPO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RuleStructureRepository extends JpaRepository<RuleStructureResPO, Integer> {
    RuleStructureResPO save(RuleStructureResPO ruleStructureResPO);

    RuleStructureResPO getById(Integer id);
}
