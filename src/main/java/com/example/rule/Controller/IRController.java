package com.example.rule.Controller;


import com.example.rule.Model.VO.MatchResVO;
import com.example.rule.Model.VO.TopLaws.TopLawsMatchResVO;
import com.example.rule.Service.RetrieveService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;


@RestController
@RequestMapping("/ir")
public class IRController {

    @Resource
    RetrieveService retrieveService;

    @PostMapping("/tfidf")
    public List<MatchResVO> retrieveRulesByTFIDF() {
        return retrieveService.retrieveByTFIDF();
    }

    @PostMapping("/BM25")
    public List<MatchResVO> retrieveRulesByBM25() {
        return retrieveService.retrieveByBM25();
    }

    @PostMapping("/chapter")
    public List<MatchResVO> retrieveRulesChapter() {
        return retrieveService.retrieveByChapter();
    }

    @PostMapping("/penaltyCaseTopLaws")
    public List<TopLawsMatchResVO> penaltyCaseTopLawsRetrieve() {
        return retrieveService.penaltyCaseTopLawsRetrieve();
    }

    @PostMapping("/interpretationTopLaws")
    public List<TopLawsMatchResVO> InterpretationTopLawsRetrieve() {
        return retrieveService.interpretationTopLawsRetrieve();
    }
}
