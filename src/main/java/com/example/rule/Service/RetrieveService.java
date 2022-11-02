package com.example.rule.Service;

import com.example.rule.Model.VO.MatchResVO;
import com.example.rule.Model.VO.TopLawsMatchResVO;

import java.util.List;

public interface RetrieveService {
    List<MatchResVO> retrieve();

    List<TopLawsMatchResVO> penaltyCaseTopLawsRetrieve();
}
