package com.example.rule.Dao;

import com.example.rule.Model.PO.RuleStructureResPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleStructureRepository extends JpaRepository<RuleStructureResPO, Integer> {
    List<RuleStructureResPO> findByTitle(String title);
}
