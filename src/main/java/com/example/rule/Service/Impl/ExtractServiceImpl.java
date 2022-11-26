package com.example.rule.Service.Impl;

import com.example.rule.Dao.TopLaws.TopLawsOfInterpretationRepository;
import com.example.rule.Dao.TopLaws.TopLawsOfPenaltyCaseRepository;
import com.example.rule.Dao.TopLaws.TopLawsOfRuleRepository;
import com.example.rule.Model.PO.TopLaws.TopLawsOfInterpretationPO;
import com.example.rule.Model.PO.TopLaws.TopLawsOfPenaltyCasePO;
import com.example.rule.Model.PO.TopLaws.TopLawsOfRulePO;
import com.example.rule.Service.ExtractService;
import com.example.rule.Util.IOUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Service
@Transactional
public class ExtractServiceImpl implements ExtractService {

    @Resource
    TopLawsOfRuleRepository topLawsOfRuleRepository;

    @Resource
    TopLawsOfPenaltyCaseRepository topLawsOfPenaltyCaseRepository;

    @Resource
    TopLawsOfInterpretationRepository topLawsOfInterpretationRepository;

    public void extractProprietaryVocabFormDataBase() {
        // 此处数据量较小，可以这样编写；如数据量增加还需要重构
        List<TopLawsOfRulePO> topLawsOfRulePOS = topLawsOfRuleRepository.findAll();
        List<TopLawsOfPenaltyCasePO> topLawsOfPenaltyCasePOS = topLawsOfPenaltyCaseRepository.findAll();
        List<TopLawsOfInterpretationPO> topLawsOfInterpretationPOS = topLawsOfInterpretationRepository.findAll();

        HashSet<String> pvDirectory = new HashSet<>();
        for (TopLawsOfRulePO topLawsOfRulePO : topLawsOfRulePOS) {
            pvDirectory.addAll(splitProprietaryVocabs(topLawsOfRulePO.getLaws()));
        }
        for (TopLawsOfPenaltyCasePO topLawsOfPenaltyCasePO : topLawsOfPenaltyCasePOS) {
            pvDirectory.addAll(splitProprietaryVocabs(topLawsOfPenaltyCasePO.getLaws()));
        }
        for (TopLawsOfInterpretationPO topLawsOfInterpretationPO : topLawsOfInterpretationPOS) {
            pvDirectory.addAll(splitProprietaryVocabs(topLawsOfInterpretationPO.getLaws()));
        }
        File pvDirectoryFile = new File("src/main/resources/pv_directory/银行业专有文档名词.txt");
        if (!pvDirectoryFile.exists()) {
            try {
                pvDirectoryFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        List<String> pvDirectoryList = new ArrayList<>(pvDirectory);
        pvDirectoryList.replaceAll((s) -> s + " n 100\n");
        try {
            IOUtil.writeLines(pvDirectoryFile, pvDirectoryList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void extractProprietaryVocabFormCorpus() {
        File dir = new File("F:\\魔鬼的力量\\_A研究生资料\\面向互联网+助教材料\\标准版内规");
        File[] fs = dir.listFiles();
        ArrayList<String> fileNameList = new ArrayList<>();
        for (File f : fs) {
            File[] rulesPart = f.listFiles();
            for (File file : rulesPart) {
                String fileName = file.getName();
                if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
                    fileName = fileName.replaceAll("[《》.docx]", "");
                    fileNameList.add(fileName);
                }
            }
        }

        File fileTitleFile = new File("src/main/resources/pv_directory/内规文档名名词.txt");
        fileNameList.replaceAll((s) -> s + " n 100\n");
        try {
            IOUtil.writeLines(fileTitleFile, fileNameList);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ArrayList<String> splitProprietaryVocabs(String proprietaryVocabPhrase) {
        if (proprietaryVocabPhrase.equals("")) {
            return new ArrayList<>();
        }
        ArrayList<String> proprietaryVocabs = new ArrayList<>(Arrays.asList(proprietaryVocabPhrase.split("\\|")));
        proprietaryVocabs.replaceAll((s) -> s.replaceAll("[《》]", ""));
        return proprietaryVocabs;
    }
}
