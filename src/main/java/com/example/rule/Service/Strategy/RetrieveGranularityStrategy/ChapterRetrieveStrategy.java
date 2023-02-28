package com.example.rule.Service.Strategy.RetrieveGranularityStrategy;

import com.example.rule.Dao.RuleChapterStructureRepository;
import com.example.rule.Model.Body.TermBody;
import com.example.rule.Model.Config.PathConfig;
import com.example.rule.Model.IRModel.IR_Model;
import com.example.rule.Model.PO.RuleStructureRes.RuleChapterStructureResPO;
import com.example.rule.Model.PO.RuleStructureRes.RuleStructureResPO;
import com.example.rule.Util.TermProcessingUtil;
import javafx.util.Pair;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ChapterRetrieveStrategy implements RetrieveStrategy {
    @Resource
    RuleChapterStructureRepository ruleChapterStructureRepository;

    @Override
    public Map<Integer, List<TermBody>> getTFIDFList(List<? extends RuleStructureResPO> ruleChapterStructureResPOS, IR_Model model) {
        Map<Integer, List<TermBody>> frequencyOfRulesChapter;
        Map<Integer, List<TermBody>> tfidfOfRulesChapter;
        try {
            frequencyOfRulesChapter = TermProcessingUtil.generateTermsFreq(ruleChapterStructureResPOS,
                    PathConfig.termsInfoCache + File.separator + PathConfig.chapterTermsFrequencyCache
            );
            tfidfOfRulesChapter = TermProcessingUtil.generateTermsTFIDF(frequencyOfRulesChapter,
                    PathConfig.termsInfoCache + File.separator + PathConfig.chapterTermsTFIDFCache,
                    model
            );
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return tfidfOfRulesChapter;
    }
}
