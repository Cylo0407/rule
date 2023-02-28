package com.example.rule.Dao;

import com.example.rule.Model.PO.RuleStructureRes.RuleItemStructureResPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleStructureRepository extends JpaRepository<RuleItemStructureResPO, Integer> {
    List<RuleItemStructureResPO> findByTitle(String title);
}
