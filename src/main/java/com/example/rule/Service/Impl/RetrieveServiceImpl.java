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

    @Override
    public List<MatchResVO> retrieve() {
        List<RuleStructureResPO> ruleStructureResPOS = ruleStructureRepository.findAll();
        List<PenaltyCaseStructureResPO> penaltyCaseStructureResPOS = penaltyCaseStructureRepository.findAll();
        List<InterpretationStructureResPO> interpretationStructureResPOS = interpretationStructureRepository.findAll();
        List<MatchResVO> resVOS = new ArrayList<>();

//        for (InterpretationStructureResPO interpretationStructureResPO : interpretationStructureResPOS) {
//            MatchResVO matchResVO = new MatchResVO();
//            // 1. 提取关键词
//            Map<String, Float> keywords = TextRankKeyWord.getKeyword(interpretationStructureResPO.getTitle(), interpretationStructureResPO.getText());
//            // 2. 关键词匹配
//            List<Pair<Float, Integer>> scoresIndexPair = retrievalByTFIDF(keywords, ruleStructureResPOS);
//            // 3. 分析结果
//            float maxScore = 0;
//            for (int i = 0; i < scoresIndexPair.size(); i++) {
//                maxScore = scoresIndexPair.get(i).getLeft() > maxScore ? scoresIndexPair.get(i).getLeft() : maxScore;
//            }
//
//            matchResVO.setInput_title(interpretationStructureResPO.getTitle());
//            matchResVO.setInput_text(interpretationStructureResPO.getText());
//            matchResVO.setRuleMatchRes(getListByTFIDF(ruleStructureResPOS, scoresIndexPair));
//
//            resVOS.add(matchResVO);
//        }

        for (PenaltyCaseStructureResPO penaltyCaseStructureResPO : penaltyCaseStructureResPOS) {
            MatchResVO matchResVO = new MatchResVO();
            // 1. 提取关键词
            Map<String, Float> keywords = TextRankKeyWord.getKeyword(penaltyCaseStructureResPO.getTitle(), penaltyCaseStructureResPO.getText());
            // 2. 关键词匹配
            List<Pair<Float, Integer>> scoresIndexPair = retrievalByTFIDF(keywords, ruleStructureResPOS);
            // 3. 分析结果
            float maxScore = 0;
            for (int i = 0; i < scoresIndexPair.size(); i++) {
                maxScore = scoresIndexPair.get(i).getLeft() > maxScore ? scoresIndexPair.get(i).getLeft() : maxScore;
            }

            matchResVO.setInput_title(penaltyCaseStructureResPO.getTitle());
            matchResVO.setInput_text(penaltyCaseStructureResPO.getText());
            matchResVO.setRuleMatchRes(getListByTFIDF(ruleStructureResPOS, scoresIndexPair));

            resVOS.add(matchResVO);
        }

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

    private List<Triple<Float, Integer, String>> getListByTFIDF(List<RuleStructureResPO> ruleStructureResPOS, List<Pair<Float, Integer>> scoresIndexPair) {
        Collections.sort(scoresIndexPair, new Comparator<Pair<Float, Integer>>() {
            @Override
            public int compare(Pair<Float, Integer> o1, Pair<Float, Integer> o2) {
                if (o1.getLeft() > o2.getLeft()) return -1;
                else if (o1.getLeft() < o2.getLeft()) return 1;
                else return 0;
//                return (o1.getLeft() - o2.getLeft() > 0 ? -1 : 1);
            }
        });
        List<Triple<Float, Integer, String>> res = new ArrayList<>();
        int count = 0;
        for (Pair<Float, Integer> pair : scoresIndexPair) {
            if (pair.getLeft() > 0) {
                RuleStructureResPO resPO = ruleStructureResPOS.get(pair.getRight());
                res.add(Triple.of(pair.getLeft(), resPO.getId(), resPO.getText()));
                count++;
                if (count >= 10) return res;
            }
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
