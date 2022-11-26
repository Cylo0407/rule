package com.example.rule.Dao.TopLaws;

import com.example.rule.Model.PO.TopLaws.TopLawsOfRulePO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopLawsOfRuleRepository extends JpaRepository<TopLawsOfRulePO, Integer> {
    TopLawsOfRulePO findByTitle(String title);
}
