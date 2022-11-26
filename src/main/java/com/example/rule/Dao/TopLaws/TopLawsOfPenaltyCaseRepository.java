package com.example.rule.Dao.TopLaws;

import com.example.rule.Model.PO.TopLaws.TopLawsOfPenaltyCasePO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopLawsOfPenaltyCaseRepository extends JpaRepository<TopLawsOfPenaltyCasePO, Integer> {
    TopLawsOfPenaltyCasePO findByDocId(String docId);
}
