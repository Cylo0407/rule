package com.example.rule.Service.Strategy.RetrieveGranularityStrategy;

import com.example.rule.Dao.RuleChapterStructureRepository;
import com.example.rule.Model.Body.TermBody;
import com.example.rule.Model.Config.PathConfig;
import com.example.rule.Model.IRModel.IR_Model;
import com.example.rule.Model.PO.RuleStructureRes.RuleStructureResPO;
import com.example.rule.Model.VO.MatchResVO;
import com.example.rule.Util.IOUtil;
import com.example.rule.Util.TermProcessingUtil;
import org.springframework.data.jpa.repository.JpaRepository;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ChapterRetrieveStrategy implements RetrieveStrategy {

    @Override
    public List<? extends RuleStructureResPO> findAll(JpaRepository ruleChapterStructureRepository) {
        return ((RuleChapterStructureRepository) ruleChapterStructureRepository).findAll();
    }

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

    @Override
    public void outputJson(String fileName, MatchResVO matchResVO) {
        try {
            IOUtil.createJsonRes(IOUtil.getTargetFile(PathConfig.interpretationJsonPath + "Chapter" + File.separator + fileName + ".json"), matchResVO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
