package com.example.rule.Dao;

import com.example.rule.Model.PO.RuleArticleStructureResPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleArticleStructureRepository extends JpaRepository<RuleArticleStructureResPO, Integer> {
    List<RuleArticleStructureResPO> findByTitle(String title);
}
