package com.example.rule.Dao;

import com.example.rule.Model.PO.RuleStructureRes.RuleChapterStructureResPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleChapterStructureRepository extends JpaRepository<RuleChapterStructureResPO, Integer> {
    List<RuleChapterStructureResPO> findByTitle(String title);
}
