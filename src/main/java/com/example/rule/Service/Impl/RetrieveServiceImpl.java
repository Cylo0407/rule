package com.example.rule.Service.Impl;

import com.example.rule.Dao.*;
import com.example.rule.Dao.TopLaws.TopLawsOfInterpretationRepository;
import com.example.rule.Dao.TopLaws.TopLawsOfPenaltyCaseRepository;
import com.example.rule.Dao.TopLaws.TopLawsOfRuleRepository;
import com.example.rule.Model.Body.MatchesBody;
import com.example.rule.Model.Body.TermBody;
import com.example.rule.Model.IRModel.IR_Model;
import com.example.rule.Model.IRModel.VSM;
import com.example.rule.Model.PO.*;
import com.example.rule.Model.PO.TopLaws.TopLawsOfInterpretationPO;
import com.example.rule.Model.PO.TopLaws.TopLawsOfPenaltyCasePO;
import com.example.rule.Model.PO.TopLaws.TopLawsOfRulePO;
import com.example.rule.Model.VO.MatchResVO;
import com.example.rule.Model.VO.TopLaws.TopLawsMatchResVO;
import com.example.rule.Service.RetrieveService;
import com.example.rule.Util.TermProcessingUtil;
import com.example.rule.Model.IRModel.Algorithm.BM25;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;


@Service
@Transactional
public class RetrieveServiceImpl implements RetrieveService {

    @Resource
    RuleStructureRepository ruleStructureRepository;
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

    /**
     * 计算每一条法规解释与内规之间的相似度
     *
     * @return resVO: 每一条解释与内规之间的相似度
     */
    @Override
    public List<MatchResVO> retrieveByTFIDF() {
        this.setModel(new VSM());
        return this.retrieve("rule");
    }

    /**
     * 通过BM25算法计算政策解读与内规之间的相似度
     * 1、单词Wi的权重，即idf；
     * 2、单词与文档之间的相关性；
     * 3、单词与Query之间的相关性；
     */
    @Override
    public List<MatchResVO> retrieveByBM25() {
        this.setModel(new BM25());
        return this.retrieve("rule");
    }

    /**
     * 通过BM25算法计算政策解读与内规章节总体之间的相似度
     */
    @Override
    public List<MatchResVO> retrieveByChapter() {
        this.setModel(new BM25());
        return this.retrieve("ruleChapter");
    }

    @Override
    public List<MatchResVO> retrieveByArticle() {
        this.setModel(new BM25());
        return this.retrieve("ruleArticle");
    }

    private List<MatchResVO> retrieve(String granularity) {
        // 读取内规库和输入库
        List<RuleStructureResPO> ruleStructureResPOS = ruleStructureRepository.findAll();
        List<RuleChapterStructureResPO> ruleChapterStructureResPOS = ruleChapterStructureRepository.findAll();
        List<RuleArticleStructureResPO> ruleArticleStructureResPOS = ruleArticleStructureRepository.findAll();

        List<InterpretationStructureResPO> interpretationStructureResPOS = interpretationStructureRepository.findAll();

        List<MatchResVO> resVOS = new ArrayList<>();

        //frequencyOfRules: <ruleId,<keyword,frequency>>
        Map<Integer, List<TermBody>> frequencyOfRules = null;
        Map<Integer, List<TermBody>> frequencyOfRulesChapter = null;
        Map<Integer, List<TermBody>> frequencyOfRulesArticle = null;
        //tfidfOfRules: <ruleId,<keyword,tfidf>>
        Map<Integer, List<TermBody>> tfidfOfRules = null;
        Map<Integer, List<TermBody>> tfidfOfRulesChapter = null;
        Map<Integer, List<TermBody>> tfidfOfRulesArticle = null;
        try {
            // 获取or生成内规的tfidf映射
            switch (granularity) {
                case "rule":
                    frequencyOfRules = TermProcessingUtil.generateTermsFreq(ruleStructureResPOS);
                    tfidfOfRules = TermProcessingUtil.generateTermsTFIDF(frequencyOfRules, this.model);
                    break;
                case "ruleChapter":
                    frequencyOfRulesChapter = TermProcessingUtil.generateTermsFreq(ruleChapterStructureResPOS);
                    tfidfOfRulesChapter = TermProcessingUtil.generateTermsTFIDF(frequencyOfRulesChapter, this.model);
                    break;
                case "ruleArticle":
                    frequencyOfRulesArticle = TermProcessingUtil.generateTermsFreq(ruleArticleStructureResPOS);
                    tfidfOfRulesArticle = TermProcessingUtil.generateTermsTFIDF(frequencyOfRulesArticle, this.model);
                    break;
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        for (InterpretationStructureResPO interpretationStructureResPO : interpretationStructureResPOS) {
            MatchResVO matchResVO = new MatchResVO();
            // 1. 对输入进行分词
            List<TermBody> inputTermBodies = TermProcessingUtil.calTermFreq(interpretationStructureResPO.getText());
            Map<Integer, Double> similarityBetweenInputAndRules = null;
            List<MatchesBody> matchesBodyList = null;
            switch (granularity) {
                case "rule":
                    // 2. 计算输入中每个词的TF-IDF值
                    this.model.calTermsWeight(inputTermBodies, tfidfOfRules);
                    // sims：<ruleId，similarity>
                    similarityBetweenInputAndRules = TermProcessingUtil.calSimilarity(inputTermBodies, tfidfOfRules);
                    matchesBodyList = similarityToResult(similarityBetweenInputAndRules, ruleStructureResPOS);
                    break;
                case "ruleChapter":
                    this.model.calTermsWeight(inputTermBodies, tfidfOfRulesChapter);
                    similarityBetweenInputAndRules = TermProcessingUtil.calSimilarity(inputTermBodies, tfidfOfRulesChapter);
                    matchesBodyList = similarityToResult(similarityBetweenInputAndRules, ruleChapterStructureResPOS);
                    break;
                case "ruleArticle":
                    this.model.calTermsWeight(inputTermBodies, tfidfOfRulesArticle);
                    similarityBetweenInputAndRules = TermProcessingUtil.calSimilarity(inputTermBodies, tfidfOfRulesArticle);
                    matchesBodyList = similarityToResult(similarityBetweenInputAndRules, ruleArticleStructureResPOS);
                    break;
            }
            sortResultBySimilarity(Objects.requireNonNull(matchesBodyList));
            matchResVO.setInput_fileName(interpretationStructureResPO.getTitle());
            matchResVO.setInput_text(interpretationStructureResPO.getText());
            matchResVO.setRuleMatchRes(matchesBodyList);
            resVOS.add(matchResVO);
        }
        return resVOS;
    }

    private List<MatchesBody> similarityToResult(Map<Integer, Double> sims, List<?> resPOS) {
        List<MatchesBody> res = new ArrayList<>();
        for (Object po : resPOS) {
            MatchesBody ruleResMatch = null;
            if (po instanceof RuleStructureResPO) {
                ruleResMatch = getMatchesBody(sims, (RuleStructureResPO) po);
            } else if (po instanceof RuleChapterStructureResPO) {
                ruleResMatch = getMatchesBody(sims, (RuleChapterStructureResPO) po);
            } else if (po instanceof RuleArticleStructureResPO) {
                ruleResMatch = getMatchesBody(sims, (RuleArticleStructureResPO) po);
            }
            if (ruleResMatch != null && ruleResMatch.getSimilarity() > 0.01) {
                res.add(ruleResMatch);
            }
        }
        return res;
    }

    private MatchesBody getMatchesBody(Map<Integer, Double> sims, RuleStructureResPO po) {
        Integer id = po.getId();
        Double similarity = sims.get(id);
        return new MatchesBody(similarity, po.getTitle(), po.getText(), 0);

    }

    /**
     * @param sims 键值对<id,similarity>
     * @param po   章节
     * @return 通过键值对找到对应的章节体
     */
    private MatchesBody getMatchesBody(Map<Integer, Double> sims, RuleChapterStructureResPO po) {
        if (po.getText() == null) {
            return null;
        }
        Integer id = po.getId();
        Double similarity = sims.get(id);
        return new MatchesBody(similarity, po.getTitle(), po.getChapter() + '\n' + po.getText(), 0);
    }

    private MatchesBody getMatchesBody(Map<Integer, Double> sims, RuleArticleStructureResPO po) {
        if (po.getText() == null) {
            return null;
        }
        Integer id = po.getId();
        Double similarity = sims.get(id);
        return new MatchesBody(similarity, po.getTitle(), po.getText(), 0);
    }

    private void sortResultBySimilarity(List<MatchesBody> matchesBodyList) {
        matchesBodyList.sort((o1, o2) -> o2.getSimilarity().compareTo(o1.getSimilarity()));
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

    private List<Triple<Double, Integer, Triple<String, String, String>>> getListBySim2(List<Pair<RuleStructureResPO, Double>> sims) {
        Collections.sort(sims, new Comparator<Pair<RuleStructureResPO, Double>>() {
            @Override
            public int compare(Pair<RuleStructureResPO, Double> o1, Pair<RuleStructureResPO, Double> o2) {
                if (o1.getRight() > o2.getRight()) return -1;
                else if (o1.getRight() < o2.getRight()) return 1;
                else return 0;
            }
        });

        List<Triple<Double, Integer, Triple<String, String, String>>> res = new ArrayList<>();
        int count = 0;
        for (Pair<RuleStructureResPO, Double> pair : sims) {
            if (pair.getRight() > 0) {
                RuleStructureResPO ruleStructureResPO = pair.getLeft();
                System.out.println(ruleStructureResPO.getText() + "----" + pair.getRight());
                // triple：<similarity,ruleId,ruleContent>
                String title = ruleStructureResPO.getTitle();
                TopLawsOfRulePO topLawsOfRulePO = topLawsOfRuleRepository.findByTitle(title);
                res.add(Triple.of(pair.getRight(), ruleStructureResPO.getId(), Triple.of(ruleStructureResPO.getTitle(), topLawsOfRulePO.getLaws(), ruleStructureResPO.getText())));
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
}
