package com.example.rule.Dao;

import com.example.rule.Model.PO.RuleChpterStructureResPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleChapterStructureRepository extends JpaRepository<RuleChpterStructureResPO, Integer> {
    List<RuleChpterStructureResPO> findByTitle(String title);
}
