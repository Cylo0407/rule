package com.example.rule.Controller;

import com.example.rule.Service.ExtractService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/extract")
public class ExtractController {
    @Resource
    ExtractService extractService;

    @PostMapping("/extractPVFromDB")
    public boolean extractPVFromDB() {
        extractService.extractProprietaryVocabFormDataBase();
        return true;
    }

    @PostMapping("/extractPVFromCorpus")
    public boolean extractPVFromCorpus() {
        extractService.extractProprietaryVocabFormCorpus();
        return true;
    }
}
