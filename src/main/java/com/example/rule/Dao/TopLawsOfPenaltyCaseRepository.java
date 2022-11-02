package com.example.rule.Dao;

import com.example.rule.Model.PO.TopLawsOfPenaltyCasePO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopLawsOfPenaltyCaseRepository extends JpaRepository<TopLawsOfPenaltyCasePO, Integer> {
    TopLawsOfPenaltyCasePO findByDocId(String docId);
}
