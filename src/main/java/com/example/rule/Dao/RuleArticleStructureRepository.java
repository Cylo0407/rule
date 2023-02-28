package com.example.rule.Dao;

import com.example.rule.Model.PO.RuleStructureRes.RuleArticleStructureResPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleArticleStructureRepository extends JpaRepository<RuleArticleStructureResPO, Integer> {
    List<RuleArticleStructureResPO> findByTitle(String title);
}
