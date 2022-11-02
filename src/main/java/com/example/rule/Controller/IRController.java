package com.example.rule.Controller;


import com.example.rule.Model.VO.MatchResVO;
import com.example.rule.Model.VO.TopLawsMatchResVO;
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

    @PostMapping("/retrieval")
    public List<MatchResVO> retrieveRules() {
        return retrieveService.retrieve();
    }

    @PostMapping("/penaltyCaseTopLaws")
    public List<TopLawsMatchResVO> penaltyCaseTopLawsRetrieve() {
        return retrieveService.penaltyCaseTopLawsRetrieve();
    }
}
