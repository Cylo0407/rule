package com.example.rule.Service.Strategy.RetrieveGranularityStrategy;

import com.example.rule.Dao.RuleArticleStructureRepository;
import com.example.rule.Model.Body.TermBody;
import com.example.rule.Model.Config.PathConfig;
import com.example.rule.Model.IRModel.IR_Model;
import com.example.rule.Model.PO.RuleStructureRes.RuleArticleStructureResPO;
import com.example.rule.Model.PO.RuleStructureRes.RuleStructureResPO;
import com.example.rule.Util.TermProcessingUtil;
import javafx.util.Pair;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ArticleRetrieveStrategy implements RetrieveStrategy {

    @Resource
    RuleArticleStructureRepository ruleArticleStructureRepository;

    @Override
    public Map<Integer, List<TermBody>> getTFIDFList(List<? extends RuleStructureResPO> ruleArticleStructureResPOS, IR_Model model) {
        Map<Integer, List<TermBody>> frequencyOfRulesArticle;
        Map<Integer, List<TermBody>> tfidfOfRulesArticle = null;
        try {
            frequencyOfRulesArticle = TermProcessingUtil.generateTermsFreq(ruleArticleStructureResPOS,
                    PathConfig.termsInfoCache + File.separator + PathConfig.articleTermsFrequencyCache
            );
            tfidfOfRulesArticle = TermProcessingUtil.generateTermsTFIDF(frequencyOfRulesArticle,
                    PathConfig.termsInfoCache + File.separator + PathConfig.articleTermsTFIDFCache,
                    model
            );
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return tfidfOfRulesArticle;
    }
}
