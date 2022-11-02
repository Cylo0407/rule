package com.example.rule.Service.Impl;

import com.example.rule.Dao.InterpretationStructureRepository;
import com.example.rule.Dao.PenaltyCaseStructureRepository;
import com.example.rule.Dao.RuleStructureRepository;
import com.example.rule.Model.PO.InterpretationStructureResPO;
import com.example.rule.Model.PO.PenaltyCaseStructureResPO;
import com.example.rule.Model.PO.RuleStructureResPO;
import com.example.rule.Model.VO.MatchResVO;
import com.example.rule.Service.RetrieveService;
import com.example.rule.Util.IOUtil;
import com.example.rule.Util.TextRankKeyWord;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.*;
import java.nio.file.Files;
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
        interpretationStructureResPOS = interpretationStructureResPOS.subList(0, 3);

        List<MatchResVO> resVOS = new ArrayList<>();

        //tfidfOfRules: <ruleId,<keyward,tfidf>>
        Map<RuleStructureResPO, Map<String, Double>> tfidfOfRules = new HashMap<>();
        //frequencyOfRules: <ruleId,<keyward,frequency>>
        Map<RuleStructureResPO, Map<String, Integer>> frequencyOfRules = new HashMap<>();

        // TODO 把这一步计算出的结果保存下来，以免重复计算：需要保存词频表和tf-idf表
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
//        for (PenaltyCaseStructureResPO penaltyCaseStructureResPO : penaltyCaseStructureResPOS) {
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
            matchResVO.setInput_title(interpretationStructureResPO.getTitle());
            matchResVO.setInput_text(interpretationStructureResPO.getText());
            matchResVO.setRuleMatchRes(getListBySim(similarityBetweenInputAndRules));

            resVOS.add(matchResVO);
        }

        return resVOS;
    }


    private List<Triple<Double, Integer, Pair<String, String>>> getListBySim(List<Pair<RuleStructureResPO, Double>> sims) {
        Collections.sort(sims, new Comparator<Pair<RuleStructureResPO, Double>>() {
            @Override
            public int compare(Pair<RuleStructureResPO, Double> o1, Pair<RuleStructureResPO, Double> o2) {
                if (o1.getRight() > o2.getRight()) return -1;
                else if (o1.getRight() < o2.getRight()) return 1;
                else return 0;
            }
        });

        List<Triple<Double, Integer, Pair<String, String>>> res = new ArrayList<>();
        int count = 0;
        for (Pair<RuleStructureResPO, Double> pair : sims) {
            if (pair.getRight() > 0) {
                RuleStructureResPO ruleStructureResPO = pair.getLeft();
                System.out.println(ruleStructureResPO.getText() + "----" + pair.getRight());
                // triple：<similarity,ruleId,ruleContent>
                res.add(Triple.of(pair.getRight(), ruleStructureResPO.getId(), Pair.of(ruleStructureResPO.getTitle(), ruleStructureResPO.getText())));
                count++;
                // 限制输出15条相关内容
                if (count >= 20) return res;
            }
        }
        return res;
    }
}
