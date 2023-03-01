package com.example.rule.Service.Strategy.RetrieveGranularityStrategy;

import com.example.rule.Dao.RuleStructureRepository;
import com.example.rule.Model.Body.TermBody;
import com.example.rule.Model.Config.PathConfig;
import com.example.rule.Model.IRModel.IR_Model;
import com.example.rule.Model.PO.RuleStructureRes.RuleItemStructureResPO;
import com.example.rule.Model.PO.RuleStructureRes.RuleStructureResPO;
import com.example.rule.Util.TermProcessingUtil;
import javafx.util.Pair;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemRetrieveStrategy implements RetrieveStrategy {
    @Resource
    RuleStructureRepository ruleStructureRepository;

    @Override
    public Map<Integer, List<TermBody>> getTFIDFList(List<? extends RuleStructureResPO> ruleItemStructureResPOS, IR_Model model) {

        Map<Integer, List<TermBody>> frequencyOfRules;
        Map<Integer, List<TermBody>> tfidfOfRules;
        try {
            frequencyOfRules = TermProcessingUtil.generateTermsFreq(ruleItemStructureResPOS,
                    PathConfig.termsInfoCache + File.separator + PathConfig.termsFrequencyCache
            );
            tfidfOfRules = TermProcessingUtil.generateTermsTFIDF(frequencyOfRules,
                    PathConfig.termsInfoCache + File.separator + PathConfig.termsTFIDFCache,
                    model
            );
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return tfidfOfRules;
    }

}
