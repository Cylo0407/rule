package com.example.rule.Controller;


import com.example.rule.Model.VO.MatchResVO;
import com.example.rule.Model.VO.TopLaws.TopLawsMatchResVO;
import com.example.rule.Service.RetrieveService;
import org.springframework.web.bind.annotation.PathVariable;
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
    public Boolean retrieveRulesByTFIDF() {
        return retrieveService.retrieveByTFIDF();
    }

    @PostMapping("/retrieveByItem")
    public Boolean retrieveRulesByBM25() {
        return retrieveService.retrieveByBM25();
    }

    @PostMapping("/retrieveByChapter")
    public Boolean retrieveRulesChapter() {
        return retrieveService.retrieveByChapter();
    }

    @PostMapping("/retrieveByArticle")
    public Boolean retrieveRulesArticle() {
        return retrieveService.retrieveByArticle();
    }

    @PostMapping("/doRetrieve/{granularity}/{termWeight}")
    public Boolean doRetrieve(@PathVariable String granularity, @PathVariable int termWeight) {
        return retrieveService.doRetrieve(granularity, termWeight);
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
