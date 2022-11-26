package com.example.rule.Dao.TopLaws;

import com.example.rule.Model.PO.TopLaws.TopLawsOfInterpretationPO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopLawsOfInterpretationRepository extends JpaRepository<TopLawsOfInterpretationPO,Integer> {
    TopLawsOfInterpretationPO findByDocId(String doc_id);
}
