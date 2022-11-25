package com.example.rule.Service.Impl;

import com.example.rule.Dao.*;
import com.example.rule.Model.Body.MatchesBody;
import com.example.rule.Model.PO.*;
import com.example.rule.Model.VO.MatchResVO;
import com.example.rule.Model.VO.TopLawsMatchResVO;
import com.example.rule.Service.RetrieveService;
import com.example.rule.Util.IOUtil;
import com.example.rule.Util.TextRankKeyWord;
import com.example.rule.Util.BM25;
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
    PenaltyCaseStructureRepository penaltyCaseStructureRepository;
    @Resource
    TopLawsOfPenaltyCaseRepository topLawsOfPenaltyCaseRepository;
    @Resource
    InterpretationStructureRepository interpretationStructureRepository;
    @Resource
    TopLawsOfInterpretationRepository topLawsOfInterpretationRepository;


    /**
     * 计算每一条法规解释与内规之间的相似度
     * TODO 可以优化的地方在于，循环中访问数据库和重复调用分词的部分
     *
     * @return resVO: 每一条解释与内规之间的相似度
     */
    @Override
    public List<MatchResVO> retrieveByTFIDF() {
        List<RuleStructureResPO> ruleStructureResPOS = ruleStructureRepository.findAll();
        List<InterpretationStructureResPO> interpretationStructureResPOS = interpretationStructureRepository.findAll();

        List<MatchResVO> resVOS = new ArrayList<>();

        //tfidfOfRules: <ruleId,<keyward,tfidf>>
        Map<RuleStructureResPO, Map<String, Double>> tfidfOfRules = new HashMap<>();
        //frequencyOfRules: <ruleId,<keyward,frequency>>
        Map<RuleStructureResPO, Map<String, Integer>> frequencyOfRules = new HashMap<>();

        // 保存词频表和tf-idf表
        System.out.println("start rule");
        File rulesWordsFrequency = new File("src/main/resources/rules_info/rules_word_frequency.txt");
        if (rulesWordsFrequency.exists()) {
            try {
                frequencyOfRules = (Map<RuleStructureResPO, Map<String, Integer>>) IOUtil.readObject(rulesWordsFrequency);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            // 对内规库里检索到的每条内规执行如下：
            for (RuleStructureResPO ruleStructureResPO : ruleStructureResPOS) {
                // 获取一条内规的词频
                Map<String, Integer> ruleFrequency = TextRankKeyWord.getWordList(ruleStructureResPO.getTitle(), ruleStructureResPO.getText());
                //存储内规词频
                frequencyOfRules.put(ruleStructureResPO, ruleFrequency);
            }
            try {
                rulesWordsFrequency.createNewFile();
                IOUtil.writeObject(rulesWordsFrequency, frequencyOfRules);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("end fre");
        File rulesWordsTFIDF = new File("src/main/resources/rules_info/rules_word_tfidf.txt");
        if (rulesWordsTFIDF.exists()) {
            try {
                tfidfOfRules = (Map<RuleStructureResPO, Map<String, Double>>) IOUtil.readObject(rulesWordsTFIDF);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            for (RuleStructureResPO ruleStructureResPO : ruleStructureResPOS) {
                // 计算一条内规的TF-IDF
                Map<String, Double> tfidfOfRule =
                        TextRankKeyWord.getKeyWords(frequencyOfRules.get(ruleStructureResPO), frequencyOfRules);
                tfidfOfRules.put(ruleStructureResPO, tfidfOfRule);
            }
            try {
                rulesWordsTFIDF.createNewFile();
                IOUtil.writeObject(rulesWordsTFIDF, tfidfOfRules);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("end tfidf");


        //针对外部输入进行检索:
        for (InterpretationStructureResPO interpretationStructureResPO : interpretationStructureResPOS) {
            MatchResVO matchResVO = new MatchResVO();
            // 1. 分词
            Map<String, Integer> inputFrequency = TextRankKeyWord.getWordList("", interpretationStructureResPO.getText());
            // 2. 计算每个词的TF-IDF值
            Map<String, Double> tfidfOfInput = TextRankKeyWord.getKeyWords(inputFrequency, frequencyOfRules);
            // sims：<ruleId，similarity>
            List<Pair<RuleStructureResPO, Double>> similarityBetweenInputAndRules = new ArrayList<>();

            System.out.println("retreval");
            for (Map.Entry<RuleStructureResPO, Map<String, Double>> entry : tfidfOfRules.entrySet()) {
                Map<String, Double> weight = entry.getValue();
                Set<String> keywords = new HashSet<>();
                // 计算向量模
                double a = 0.0;
                for (Map.Entry<String, Double> me : tfidfOfInput.entrySet()) {
                    keywords.add(me.getKey());
                    a += me.getValue() * me.getValue();
                }
                a = Math.sqrt(a);
                double b = 0.0;
                for (Map.Entry<String, Double> me : weight.entrySet()) {
                    keywords.add(me.getKey());
                    b += me.getValue() * me.getValue();
                }
                b = Math.sqrt(b);

                // 计算向量点积
                double ab = 0.0;
                for (String word : keywords) {
                    ab += tfidfOfInput.getOrDefault(word, 0.0) * weight.getOrDefault(word, 0.0);
                }
                double cos = ab / (a * b);
                similarityBetweenInputAndRules.add(Pair.of(entry.getKey(), cos));
            }
            matchResVO.setInput_fileName(interpretationStructureResPO.getTitle());
            matchResVO.setInput_text(interpretationStructureResPO.getText());
            matchResVO.setRuleMatchRes(getListBySim(similarityBetweenInputAndRules));

            resVOS.add(matchResVO);
        }

        return resVOS;
    }


    private List<MatchesBody> getListBySim(List<Pair<RuleStructureResPO, Double>> sims) {
        sims.sort((o1, o2) -> o2.getRight().compareTo(o1.getRight()));

        List<MatchesBody> res = new ArrayList<>();
        for (Pair<RuleStructureResPO, Double> pair : sims) {
            if (pair.getRight() > 0) {
                RuleStructureResPO ruleStructureResPO = pair.getLeft();
                // triple：<similarity,ruleId,ruleContent>
                res.add(new MatchesBody(pair.getRight(), ruleStructureResPO.getTitle(), ruleStructureResPO.getText(), 0));
            }
        }
        return res;
    }


    /**
     * 通过BM25算法计算政策解读与内规之间的相似度
     * TODO : BM25算法主要有三部分
     * 1、单词Wi的权重，即idf；
     * 2、单词与文档之间的相关性；
     * 3、单词与Query之间的相关性；
     */
    @Override
    public List<MatchResVO> retrieveByBM25() {
        List<RuleStructureResPO> ruleStructureResPOS = ruleStructureRepository.findAll();
        List<InterpretationStructureResPO> interpretationStructureResPOS = interpretationStructureRepository.findAll();

        List<MatchResVO> resVOS = new ArrayList<>();

        //frequencyOfRules: <ruleId,<keyward,frequency>>
        Map<RuleStructureResPO, Map<String, Integer>> frequencyOfRules = new HashMap<>();
        //tfidfOfRules: <ruleId,<keyward,tfidf>>
        Map<RuleStructureResPO, Map<String, Double>> BM25_tfidfOfRules = new HashMap<>();

        // TODO 把这一步计算出的结果保存下来，以免重复计算：需要保存词频表和tf-idf表
        File rulesWordsFrequency = new File("src/main/resources/rules_info/rules_word_frequency.txt");
        if (rulesWordsFrequency.exists()) {
            try {
                frequencyOfRules = (Map<RuleStructureResPO, Map<String, Integer>>) IOUtil.readObject(rulesWordsFrequency);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            // 对内规库里检索到的每条内规执行如下：
            for (RuleStructureResPO ruleStructureResPO : ruleStructureResPOS) {
                // 获取一条内规的词频
                Map<String, Integer> ruleFrequency = TextRankKeyWord.getWordList(ruleStructureResPO.getTitle(), ruleStructureResPO.getText());
                //存储内规词频
                frequencyOfRules.put(ruleStructureResPO, ruleFrequency);
            }
            try {
                rulesWordsFrequency.createNewFile();
                IOUtil.writeObject(rulesWordsFrequency, frequencyOfRules);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        File rulesWordsTFIDF = new File("src/main/resources/rules_info/rules_word_tfidf.txt");
        if (rulesWordsTFIDF.exists()) {
            try {
                BM25_tfidfOfRules = (Map<RuleStructureResPO, Map<String, Double>>) IOUtil.readObject(rulesWordsTFIDF);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            for (RuleStructureResPO ruleStructureResPO : ruleStructureResPOS) {
                // 计算一条内规的TF-IDF
                Map<String, Double> BM_tfidfOfRule =
                        BM25.getKeyWords(frequencyOfRules.get(ruleStructureResPO), frequencyOfRules);
                BM25_tfidfOfRules.put(ruleStructureResPO, BM_tfidfOfRule);
            }
            try {
                rulesWordsTFIDF.createNewFile();
                IOUtil.writeObject(rulesWordsTFIDF, BM25_tfidfOfRules);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        //针对每一个政策解读条例:
        for (InterpretationStructureResPO interpretationStructureResPO : interpretationStructureResPOS) {
            MatchResVO matchResVO = new MatchResVO();
            // 1. 分词
            Map<String, Integer> inputFrequency = TextRankKeyWord.getWordList("", interpretationStructureResPO.getText());
            // 2. 计算每个词的TF-IDF值
            Map<String, Double> BM25_tfidfOfInput = BM25.getKeyWords(inputFrequency, frequencyOfRules);
            // sims：<ruleId，similarity>
            List<Pair<RuleStructureResPO, Double>> similarityBetweenInputAndRules = new ArrayList<>();

            for (Map.Entry<RuleStructureResPO, Map<String, Double>> entry : BM25_tfidfOfRules.entrySet()) {
                Map<String, Double> weight = entry.getValue();
                Set<String> keywords = new HashSet<>();
                // 计算向量模
                double a = 0.0;
                for (Map.Entry<String, Double> me : BM25_tfidfOfInput.entrySet()) {
                    keywords.add(me.getKey());
                    a += me.getValue() * me.getValue();
                }
                a = Math.sqrt(a);
                double b = 0.0;
                for (Map.Entry<String, Double> me : weight.entrySet()) {
                    keywords.add(me.getKey());
                    b += me.getValue() * me.getValue();
                }
                b = Math.sqrt(b);

                // 计算向量点积
                double ab = 0.0;
                for (String word : keywords) {
                    ab += BM25_tfidfOfInput.getOrDefault(word, 0.0) * weight.getOrDefault(word, 0.0);
                }
                double cos = ab / (a * b);
                similarityBetweenInputAndRules.add(Pair.of(entry.getKey(), cos));
            }
            matchResVO.setInput_fileName(interpretationStructureResPO.getTitle());
            matchResVO.setInput_text(interpretationStructureResPO.getText());
            matchResVO.setRuleMatchRes(getListBySim(similarityBetweenInputAndRules));

            resVOS.add(matchResVO);
        }

        return resVOS;
    }


    /**
     * 计算具有相同上位法的每一条处罚案例与内规之间的相似度
     *
     * @return resVO: 每一条解释与内规之间的相似度
     */
    @Override
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


    @Override
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
}
