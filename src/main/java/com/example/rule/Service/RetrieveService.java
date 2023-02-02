package com.example.rule.Service;

import com.example.rule.Model.VO.MatchResVO;
import com.example.rule.Model.VO.TopLaws.TopLawsMatchResVO;

import java.util.List;

public interface RetrieveService {
    Boolean retrieveByTFIDF();

    Boolean retrieveByBM25();

    Boolean retrieveByChapter();

    Boolean retrieveByArticle();

    List<TopLawsMatchResVO> penaltyCaseTopLawsRetrieve();

    List<TopLawsMatchResVO> interpretationTopLawsRetrieve();
}
