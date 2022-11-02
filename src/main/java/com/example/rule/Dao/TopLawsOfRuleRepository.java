package com.example.rule.Dao;

import com.example.rule.Model.PO.TopLawsOfRulePO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopLawsOfRuleRepository extends JpaRepository<TopLawsOfRulePO, Integer> {
    TopLawsOfRulePO findByTitle(String title);
}
