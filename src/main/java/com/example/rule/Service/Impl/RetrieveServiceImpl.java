package com.example.rule.Service.Impl;

import com.example.rule.Dao.InterpretationStructureRepository;
import com.example.rule.Dao.PenaltyCaseStructureRepository;
import com.example.rule.Dao.RuleStructureRepository;
import com.example.rule.Model.PO.InterpretationStructureResPO;
import com.example.rule.Model.PO.PenaltyCaseStructureResPO;
import com.example.rule.Model.PO.RuleStructureResPO;
import com.example.rule.Model.VO.MatchResVO;
import com.example.rule.Service.RetrieveService;
import com.example.rule.Util.TextRankKeyWord;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;


@Service
@Transactional
public class RetrieveServiceImpl implements RetrieveService {

    @Resource
    RuleStructureRepository ruleStructureRepository;
    @Resource
    PenaltyCaseStructureRepository penaltyCaseStructureRepository;
    @Resource
    InterpretationStructureRepository interpretationStructureRepository;

    /**
     * 计算每一条法规解释与内规之间的相似度
     * TODO 可以优化的地方在于，循环中访问数据库和重复调用分词的部分
     *
     * @return resVO：每一条解释与内规之间的相似度
     */
    @Override
    public List<MatchResVO> retrieve() {
        List<RuleStructureResPO> ruleStructureResPOS = ruleStructureRepository.findAll();
        List<PenaltyCaseStructureResPO> penaltyCaseStructureResPOS = penaltyCaseStructureRepository.findAll();
        List<InterpretationStructureResPO> interpretationStructureResPOS = interpretationStructureRepository.findAll();

        List<MatchResVO> resVOS = new ArrayList<>();

        //tfidfOfRules: <ruleId,<keyward,tfidf>>
        Map<RuleStructureResPO, Map<String, Double>> tfidfOfRules = new HashMap<>();
        //frequencyOfRules: <ruleId,<keyward,frequency>>
        Map<RuleStructureResPO, Map<String, Integer>> frequencyOfRules = new HashMap<>();

        System.out.println("start rule");
        // 对内规库里检索到的每条内规执行如下：
        for (RuleStructureResPO ruleStructureResPO : ruleStructureResPOS) {
            // 获取一条内规的词频
            Map<String, Integer> ruleFrequency = TextRankKeyWord.getWordList(ruleStructureResPO.getTitle(), ruleStructureResPO.getText());
            //存储内规词频
            frequencyOfRules.put(ruleStructureResPO, ruleFrequency);
        }
        for (RuleStructureResPO ruleStructureResPO : ruleStructureResPOS) {
            // 计算一条内规的TF-IDF
            Map<String, Double> tfidfOfRule =
                    TextRankKeyWord.getKeyWords(frequencyOfRules.get(ruleStructureResPO), frequencyOfRules);
            tfidfOfRules.put(ruleStructureResPO, tfidfOfRule);
        }
        System.out.println("end rule");

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
                double cos = ab / a * b;
                similarityBetweenInputAndRules.add(Pair.of(entry.getKey(), cos));
            }
            matchResVO.setInput_title(interpretationStructureResPO.getTitle());
            matchResVO.setInput_text(interpretationStructureResPO.getTitle());
            matchResVO.setRuleMatchRes(getListBySim(similarityBetweenInputAndRules));

            resVOS.add(matchResVO);
        }

//
//        for (PenaltyCaseStructureResPO penaltyCaseStructureResPO : penaltyCaseStructureResPOS) {
//            MatchResVO matchResVO = new MatchResVO();
//            // 1. 提取关键词
//            Map<String, Float> keywords = TextRankKeyWord.getKeyword(penaltyCaseStructureResPO.getTitle(), penaltyCaseStructureResPO.getText());
//            // 2. 关键词匹配
//            List<Pair<Float, Integer>> scoresIndexPair = retrievalByTFIDF(keywords, ruleStructureResPOS);
//            // 3. 分析结果
//            float maxScore = 0;
//            for (int i = 0; i < scoresIndexPair.size(); i++) {
//                maxScore = scoresIndexPair.get(i).getLeft() > maxScore ? scoresIndexPair.get(i).getLeft() : maxScore;
//            }
//
//            matchResVO.setInput_title(penaltyCaseStructureResPO.getTitle());
//            matchResVO.setInput_text(penaltyCaseStructureResPO.getText());
//            matchResVO.setRuleMatchRes(getListByTFIDF(ruleStructureResPOS, scoresIndexPair));
//
//            resVOS.add(matchResVO);
//        }

        return resVOS;
    }

    /**
     * 根据TF-IDF检索匹配项
     *
     * @param keywords            关键词-权重
     * @param ruleStructureResPOS 政策分割
     * @return List<Pair < Float, Integer>> 列表，得分:index，index为ruleStructureResPOS对应
     */
    private List<Pair<Float, Integer>> retrievalByTFIDF(Map<String, Float> keywords, List<RuleStructureResPO> ruleStructureResPOS) {
        List<Map<String, Float>> TFs = new ArrayList<>();
        List<Pair<Float, Integer>> scoresIndexPair = new ArrayList<>();
        Map<String, Integer> wordCounter = new HashMap<>();
        for (String keyword : keywords.keySet()) {
            wordCounter.put(keyword, 0);
        }
        int itemCnt = 0;
        for (RuleStructureResPO resPO : ruleStructureResPOS) {
            Map<String, Float> tf = new HashMap<>();

            itemCnt++;
            for (String keyword : keywords.keySet()) {
                int cnt = countStr(resPO.getText(), keyword);
                tf.put(keyword, (float) cnt / TextRankKeyWord.countWord(resPO.getText()));
//                    tf.put(keyword, (float) cnt);
                if (cnt > 0) {
                    wordCounter.put(keyword, wordCounter.get(keyword) + 1);
                }
            }

            TFs.add(tf);
        }
        for (int i = 0; i < ruleStructureResPOS.size(); i++) {
            float score = 0;
            for (String keyword : keywords.keySet()) {
                float idf = (float) Math.log((float) itemCnt / (wordCounter.get(keyword) + 1));
                if (idf < 0) {
                    idf = 0;
                }
                score += keywords.get(keyword) * TFs.get(i).get(keyword) * idf;
            }
            scoresIndexPair.add(Pair.of(score, i));
//            System.out.println("==============" + score + "==============");
//            System.out.println(ruleStructureResPOS.get(i).getText());
        }
        return scoresIndexPair;
    }

    private List<Triple<Double, Integer, String>> getListBySim(List<Pair<RuleStructureResPO, Double>> sims) {
        Collections.sort(sims, new Comparator<Pair<RuleStructureResPO, Double>>() {
            @Override
            public int compare(Pair<RuleStructureResPO, Double> o1, Pair<RuleStructureResPO, Double> o2) {
                if (o1.getRight() > o2.getRight()) return -1;
                else if (o1.getRight() < o2.getRight()) return 1;
                else return 0;
            }
        });

        List<Triple<Double, Integer, String>> res = new ArrayList<>();
        int count = 0;
        for (Pair<RuleStructureResPO, Double> pair : sims) {
            RuleStructureResPO ruleStructureResPO = pair.getLeft();
            System.out.println(ruleStructureResPO.getText() + "----" + pair.getRight());
            // triple：<similarity,ruleId,ruleContent>
            res.add(Triple.of(pair.getRight(), ruleStructureResPO.getId(), ruleStructureResPO.getText()));
            count++;
//                // 限制输出15条相关内容
//                if (count >= 15) return res;
        }
        return res;
    }


    /**
     * count how many str2 is contained in str1
     *
     * @param str1
     * @param str2
     * @return
     */
    private int countStr(String str1, String str2) {
        int counter = 0;
        while (str1.contains(str2)) {
            counter++;
            str1 = str1.substring(str1.indexOf(str2) + str2.length());
        }
        return counter;
    }
}
