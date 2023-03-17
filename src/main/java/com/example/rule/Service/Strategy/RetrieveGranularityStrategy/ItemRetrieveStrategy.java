package com.example.rule.Service.Strategy.RetrieveGranularityStrategy;

import com.example.rule.Dao.RuleItemStructureRepository;
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


public class ItemRetrieveStrategy implements RetrieveStrategy {
    @Override
    public List<? extends RuleStructureResPO> findAll(JpaRepository<? extends RuleStructureResPO, Integer> ruleItemStructureRepository) {
        return ((RuleItemStructureRepository) ruleItemStructureRepository).findAll();
    }

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

    @Override
    public void outputJson(String fileName, MatchResVO matchResVO) {
        try {
            IOUtil.createJsonRes(IOUtil.getTargetFile(PathConfig.interpretationJsonPath + "Item" + File.separator + fileName + ".json"), matchResVO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
