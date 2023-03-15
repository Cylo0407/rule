package com.example.rule.Service.Impl;

import com.example.rule.Dao.*;
import com.example.rule.Dao.TopLaws.TopLawsOfInterpretationRepository;
import com.example.rule.Dao.TopLaws.TopLawsOfPenaltyCaseRepository;
import com.example.rule.Dao.TopLaws.TopLawsOfRuleRepository;
import com.example.rule.Model.Body.MatchesBody;
import com.example.rule.Model.Body.TermBody;
import com.example.rule.Model.Config.NumberConfig;
import com.example.rule.Model.IRModel.IR_Model;
import com.example.rule.Model.IRModel.VSM;
import com.example.rule.Model.PO.*;
import com.example.rule.Model.PO.RuleStructureRes.RuleArticleStructureResPO;
import com.example.rule.Model.PO.RuleStructureRes.RuleChapterStructureResPO;
import com.example.rule.Model.PO.RuleStructureRes.RuleItemStructureResPO;
import com.example.rule.Model.PO.RuleStructureRes.RuleStructureResPO;
import com.example.rule.Model.PO.TopLaws.TopLawsOfInterpretationPO;
import com.example.rule.Model.PO.TopLaws.TopLawsOfPenaltyCasePO;
import com.example.rule.Model.PO.TopLaws.TopLawsOfRulePO;
import com.example.rule.Model.VO.MatchResVO;
import com.example.rule.Model.VO.TopLaws.TopLawsMatchResVO;
import com.example.rule.Service.RetrieveService;
import com.example.rule.Service.Strategy.RetrieveGranularityStrategy.ArticleRetrieveStrategy;
import com.example.rule.Service.Strategy.RetrieveGranularityStrategy.ChapterRetrieveStrategy;
import com.example.rule.Service.Strategy.RetrieveGranularityStrategy.ItemRetrieveStrategy;
import com.example.rule.Service.Strategy.RetrieveGranularityStrategy.RetrieveStrategy;
import com.example.rule.Service.Strategy.TermWeightStrategy.TermWeightStrategy;
import com.example.rule.Util.ConversionUtil;
import com.example.rule.Util.IOUtil;
import com.example.rule.Util.MeasureUtil;
import com.example.rule.Util.TermProcessingUtil;
import com.example.rule.Model.IRModel.Algorithm.BM25;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;


@Service
@Transactional
public class RetrieveServiceImpl implements RetrieveService {

    @Resource
    RuleItemStructureRepository ruleItemStructureRepository;
    @Resource
    TopLawsOfRuleRepository topLawsOfRuleRepository;
    @Resource
    RuleChapterStructureRepository ruleChapterStructureRepository;
    @Resource
    RuleArticleStructureRepository ruleArticleStructureRepository;
    @Resource
    PenaltyCaseStructureRepository penaltyCaseStructureRepository;
    @Resource
    TopLawsOfPenaltyCaseRepository topLawsOfPenaltyCaseRepository;
    @Resource
    InterpretationStructureRepository interpretationStructureRepository;
    @Resource
    TopLawsOfInterpretationRepository topLawsOfInterpretationRepository;

    private IR_Model model;

    private RetrieveStrategy granularityStrategy;
    private TermWeightStrategy termWeightStrategy;

    /**
     * 计算每一条法规解释与内规之间的相似度
     *
     * @return resVO: 每一条解释与内规之间的相似度
     */
    @Override
    public Boolean retrieveByTFIDF() {
        this.setModel(new VSM());
        this.setGranularityStrategy(new ItemRetrieveStrategy());
        List<RuleItemStructureResPO> ruleItemStructureResPOS = ruleItemStructureRepository.findAll();
        Map<Integer, List<TermBody>> tfidfOfRules = this.granularityStrategy.getTFIDFList(ruleItemStructureResPOS, this.model);
        return this.retrieve(ruleItemStructureResPOS, tfidfOfRules);
    }

    /**
     * 通过BM25算法计算政策解读与内规之间的相似度
     * 1、单词Wi的权重，即idf；
     * 2、单词与文档之间的相关性；
     * 3、单词与Query之间的相关性；
     */
    @Override
    public Boolean retrieveByBM25() {
        this.setModel(new BM25());
        this.setGranularityStrategy(new ItemRetrieveStrategy());
        List<RuleItemStructureResPO> ruleItemStructureResPOS = ruleItemStructureRepository.findAll();
        Map<Integer, List<TermBody>> tfidfOfRules = this.granularityStrategy.getTFIDFList(ruleItemStructureResPOS, this.model);
        return this.retrieve(ruleItemStructureResPOS, tfidfOfRules);
    }

    /**
     * 通过BM25算法计算政策解读与内规章节总体之间的相似度
     */
    @Override
    public Boolean retrieveByChapter() {
        this.setModel(new BM25());
        this.setGranularityStrategy(new ChapterRetrieveStrategy());
        List<RuleChapterStructureResPO> ruleChapterStructureResPOS = ruleChapterStructureRepository.findAll();
        Map<Integer, List<TermBody>> tfidfOfRules = this.granularityStrategy.getTFIDFList(ruleChapterStructureResPOS, this.model);
        return this.retrieve(ruleChapterStructureResPOS, tfidfOfRules);
    }

    @Override
    public Boolean retrieveByArticle() {
        this.setModel(new BM25());
        this.setGranularityStrategy(new ArticleRetrieveStrategy());
        List<RuleArticleStructureResPO> ruleArticleStructureResPOS = ruleArticleStructureRepository.findAll();
        Map<Integer, List<TermBody>> tfidfOfRules = this.granularityStrategy.getTFIDFList(ruleArticleStructureResPOS, this.model);
        return this.retrieve(ruleArticleStructureResPOS, tfidfOfRules);
    }

    @Override
    public Boolean doRetrieve(String granularity, int longTermWeight) {
        this.setModel(new BM25());
        NumberConfig.longTermWeight = longTermWeight;
        try {
            this.setGranularityStrategy((RetrieveStrategy) Class.forName(
                    "com.example.rule.Service.Strategy.RetrieveGranularityStrategy."
                            + StringUtils.capitalize(granularity) + "RetrieveStrategy").newInstance()
            );
            this.setTermWeightStrategy((TermWeightStrategy) Class.forName(
                    "com.example.rule.Service.Strategy.TermWeightStrategy.TermWeight"
                            + longTermWeight + "Strategy").newInstance()
            );
            Field repositoryField = this.getClass().getDeclaredField("rule" + StringUtils.capitalize(granularity) + "StructureRepository");
            Object repo = repositoryField.get(this);
            JpaRepository<? extends RuleStructureResPO, Integer> repository = this.ruleItemStructureRepository;
            if (repo instanceof JpaRepository) {
                repository = (JpaRepository<? extends RuleStructureResPO, Integer>) repo;
            }
            List<? extends RuleStructureResPO> ruleStructureResPOS = this.granularityStrategy.findAll(repository);
            Map<Integer, List<TermBody>> tfidfOfRules = this.granularityStrategy.getTFIDFList(ruleStructureResPOS, this.model);
            return this.retrieve(ruleStructureResPOS, tfidfOfRules);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private Boolean retrieve(List<? extends RuleStructureResPO> ruleStructureResPOS, Map<Integer, List<TermBody>> tfidfOfRules) {
        // 读取内规库和输入库
        List<InterpretationStructureResPO> interpretationStructureResPOS = interpretationStructureRepository.findAll();

        // frequencyOfRules: <ruleId,<keyword,frequency>>
        // tfidfOfRules: <ruleId,<keyword,tfidf>>

        for (InterpretationStructureResPO interpretationStructureResPO : interpretationStructureResPOS) {
            MatchResVO matchResVO = new MatchResVO();
            // 1. 对输入进行分词
            List<TermBody> inputTermBodies = TermProcessingUtil.calTermFreq(interpretationStructureResPO.getText());

            // 2. 计算输入中每个词的TF-IDF值
            this.model.calTermsWeight(inputTermBodies, tfidfOfRules);

            // sims：<ruleId，similarity>
            Map<Integer, Double> similarityBetweenInputAndRules = TermProcessingUtil.calSimilarity(inputTermBodies, tfidfOfRules);
            List<MatchesBody> matchesBodyList = ConversionUtil.similarityToResult(similarityBetweenInputAndRules, ruleStructureResPOS);

            MeasureUtil.sortResultBySimilarity(Objects.requireNonNull(matchesBodyList));
            matchResVO.setInput_fileName(interpretationStructureResPO.getTitle());
            matchResVO.setInput_text(interpretationStructureResPO.getText());
            matchResVO.setRuleMatchRes(matchesBodyList);

            this.granularityStrategy.outputJson(interpretationStructureResPO.getTitle(), matchResVO);
        }
        return true;
    }

    /**
     * 计算具有相同上位法的每一条处罚案例与内规之间的相似度
     *
     * @return resVO: 每一条解释与内规之间的相似度
     */
    public List<TopLawsMatchResVO> penaltyCaseTopLawsRetrieve() {
        List<TopLawsOfPenaltyCasePO> topLawsOfPenaltyCasePOList = topLawsOfPenaltyCaseRepository.findAll();
        List<TopLawsOfRulePO> topLawsOfRulePOList = topLawsOfRuleRepository.findAll();

        List<TopLawsMatchResVO> resVOS = new ArrayList<>();

        //遍历每整个处罚案例中的每个相关法，去找内规库中有相同相关法的内规
        for (TopLawsOfPenaltyCasePO topLawsOfPenaltyCasePO : topLawsOfPenaltyCasePOList) {
            Set<TopLawsOfRulePO> ruleOfSameTopLaws =
                    getRuleOfSameTopLaws(topLawsOfPenaltyCasePO, topLawsOfRulePOList); //如果用相同上位法就add进来

            TopLawsMatchResVO matchResVO = new TopLawsMatchResVO();
            matchResVO.setInput_title(topLawsOfPenaltyCasePO.getTitle());
            matchResVO.setTopLaws(topLawsOfPenaltyCasePO.getLaws());
            matchResVO.setRuleMatchRes(ruleOfSameTopLaws);

            resVOS.add(matchResVO);
        }

        return resVOS;
    }

    /**
     * 遍历处罚案例中的上位法，去找内规库中有相同上位法的内规
     *
     * @param topLawsOfPenaltyCasePO: 一个处罚库
     * @param topLawsOfRulePOList:    内规库集合
     * @return ruleOfSameTopLaws: 与对应处罚库具有相同上位法的内规集合
     */
    private Set<TopLawsOfRulePO> getRuleOfSameTopLaws(TopLawsOfPenaltyCasePO topLawsOfPenaltyCasePO, List<TopLawsOfRulePO> topLawsOfRulePOList) {
        String[] penaltyCaseLawList = topLawsOfPenaltyCasePO.getLaws().split("\\|");
        Set<TopLawsOfRulePO> ruleOfSameTopLaws = new HashSet<>();

        for (String penaltyCaseLaw : penaltyCaseLawList) { //对每一整个处罚案例中的每个上位法
            if (penaltyCaseLaw.equals("")) continue;
            for (TopLawsOfRulePO topLawsOfRulePO : topLawsOfRulePOList) { //对每一整个内规
                if (ruleOfSameTopLaws.contains(topLawsOfRulePO)) continue;//如果集合中有过了，直接看下一个.
                String[] ruleLawList = topLawsOfRulePO.getLaws().split("\\|");
                for (String ruleLaw : ruleLawList) {
                    if (ruleLaw.equals("")) continue;
                    String tmp1 = ruleLaw.replace("《", "");
                    tmp1 = tmp1.replace("》", "");
                    String tmp2 = penaltyCaseLaw.replace("《", "");
                    tmp2 = tmp2.replace("》", "");
                    if (tmp1.equals(tmp2) || tmp2.endsWith(tmp1) || tmp1.endsWith(tmp2))
                        ruleOfSameTopLaws.add(topLawsOfRulePO);
                }
            }
        }

        return ruleOfSameTopLaws;
    }

    public List<TopLawsMatchResVO> interpretationTopLawsRetrieve() {
        List<TopLawsOfInterpretationPO> topLawsOfInterpretationPOList = topLawsOfInterpretationRepository.findAll();
        List<TopLawsOfRulePO> topLawsOfRulePOList = topLawsOfRuleRepository.findAll();

        List<TopLawsMatchResVO> resVOS = new ArrayList<>();

        //遍历每整个处罚案例中的每个相关法，去找内规库中有相同相关法的内规
        for (TopLawsOfInterpretationPO topLawsOfInterpretationPO : topLawsOfInterpretationPOList) {
            Set<TopLawsOfRulePO> ruleOfSameTopLaws =
                    getRuleOfSameTopLaws(topLawsOfInterpretationPO, topLawsOfRulePOList); //如果用相同上位法就add进来

            TopLawsMatchResVO matchResVO = new TopLawsMatchResVO();
            matchResVO.setInput_title(topLawsOfInterpretationPO.getTitle());
            matchResVO.setTopLaws(topLawsOfInterpretationPO.getLaws());
            matchResVO.setRuleMatchRes(ruleOfSameTopLaws);

            resVOS.add(matchResVO);
        }
        return resVOS;
    }

    private Set<TopLawsOfRulePO> getRuleOfSameTopLaws(TopLawsOfInterpretationPO topLawsOfInterpretationPO, List<TopLawsOfRulePO> topLawsOfRulePOList) {
        String[] interpretationLawList = topLawsOfInterpretationPO.getLaws().split("\\|");
        Set<TopLawsOfRulePO> ruleOfSameTopLaws = new HashSet<>();

        for (String interpretationLaw : interpretationLawList) { //对每一整个处罚案例中的每个上位法
            if (interpretationLaw.equals("")) continue;
            for (TopLawsOfRulePO topLawsOfRulePO : topLawsOfRulePOList) { //对每一整个内规
                if (ruleOfSameTopLaws.contains(topLawsOfRulePO)) continue;//如果集合中有过了，直接看下一个.
                String[] ruleLawList = topLawsOfRulePO.getLaws().split("\\|");
                for (String ruleLaw : ruleLawList) {
                    if (ruleLaw.equals("")) continue;
                    String tmp1 = ruleLaw.replace("《", "");
                    tmp1 = tmp1.replace("》", "");
                    String tmp2 = interpretationLaw.replace("《", "");
                    tmp2 = tmp2.replace("》", "");
                    if (tmp1.equals(tmp2) || tmp2.endsWith(tmp1) || tmp1.endsWith(tmp2))
                        ruleOfSameTopLaws.add(topLawsOfRulePO);
                }
            }
        }

        return ruleOfSameTopLaws;
    }

    private List<Triple<Double, Integer, Triple<String, String, String>>> getListBySim2(List<Pair<RuleItemStructureResPO, Double>> sims) {
        Collections.sort(sims, new Comparator<Pair<RuleItemStructureResPO, Double>>() {
            @Override
            public int compare(Pair<RuleItemStructureResPO, Double> o1, Pair<RuleItemStructureResPO, Double> o2) {
                if (o1.getRight() > o2.getRight()) return -1;
                else if (o1.getRight() < o2.getRight()) return 1;
                else return 0;
            }
        });

        List<Triple<Double, Integer, Triple<String, String, String>>> res = new ArrayList<>();
        int count = 0;
        for (Pair<RuleItemStructureResPO, Double> pair : sims) {
            if (pair.getRight() > 0) {
                RuleItemStructureResPO ruleItemStructureResPO = pair.getLeft();
                System.out.println(ruleItemStructureResPO.getText() + "----" + pair.getRight());
                // triple：<similarity,ruleId,ruleContent>
                String title = ruleItemStructureResPO.getTitle();
                TopLawsOfRulePO topLawsOfRulePO = topLawsOfRuleRepository.findByTitle(title);
                res.add(Triple.of(pair.getRight(), ruleItemStructureResPO.getId(), Triple.of(ruleItemStructureResPO.getTitle(), topLawsOfRulePO.getLaws(), ruleItemStructureResPO.getText())));
                count++;
                // 限制输出15条相关内容
                if (count >= 20) return res;
            }
        }
        return res;
    }

    public void setModel(IR_Model model) {
        this.model = model;
    }

    public IR_Model getModel() {
        return model;
    }

    public RetrieveStrategy getGranularityStrategy() {
        return granularityStrategy;
    }

    public void setGranularityStrategy(RetrieveStrategy granularityStrategy) {
        this.granularityStrategy = granularityStrategy;
    }

    public TermWeightStrategy getTermWeightStrategy() {
        return termWeightStrategy;
    }

    public void setTermWeightStrategy(TermWeightStrategy termWeightStrategy) {
        this.termWeightStrategy = termWeightStrategy;
    }
}
