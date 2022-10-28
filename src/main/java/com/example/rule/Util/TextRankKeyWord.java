package com.example.rule.Util;

import com.example.rule.Model.PO.RuleStructureResPO;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary;
import com.hankcs.hanlp.seg.common.Term;

import java.util.*;

/**
 * TextRank关键词提取
 *
 * @author hankcs
 */
public class TextRankKeyWord {
    public static final int nKeyword = 10;
    /**
     * 阻尼系数（ＤａｍｐｉｎｇＦａｃｔｏｒ），一般取值为0.85
     */
    static final float d = 0.85f;
    /**
     * 最大迭代次数
     */
    static final int max_iter = 200;
    static final float min_diff = 0.001f;

    public TextRankKeyWord() {
        // jdk bug : Exception in thread "main" java.lang.IllegalArgumentException: Comparison method violates its general contract!
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
    }

    public static int countWord(String content) {
        List<Term> termList = HanLP.segment(content);
        List<String> wordList = new ArrayList<String>();
        for (Term t : termList) {
            if (shouldInclude(t)) {
                wordList.add(t.word);
            }
        }
        return wordList.size();
    }

    public static Map<String, Integer> getWordList(String title, String content) {
        List<Term> termList = HanLP.segment(title + content);
        List<String> wordList = new ArrayList<String>();
        for (Term t : termList) {
            if (shouldInclude(t)) {
                wordList.add(t.word);
            }
        }

        Map<String, Integer> frequency = new HashMap<>();
        for (String word : wordList) {
            frequency.put(word, frequency.getOrDefault(word, 0) + 1);
        }
        return frequency;
    }

    /**
     * 是否应当将这个term纳入计算，词性属于名词、动词、副词、形容词
     *
     * @param term
     * @return 是否应当
     */
    public static boolean shouldInclude(Term term) {
        return CoreStopWordDictionary.shouldInclude(term);
    }


    public static Map<String, Double> getKeyWords(Map<String, Integer> frequency, List<RuleStructureResPO> ruleStructureResPOS) {
        int maxFreq = 0;
        for (String key : frequency.keySet()) {
            if (frequency.get(key) > maxFreq) maxFreq = frequency.get(key);
        }
        Map<String, Double> tf = new HashMap<>();
        for (String key : frequency.keySet()) {
            tf.put(key, (double) frequency.get(key) / maxFreq);
        }

        Map<String, Double> weight = new HashMap<>();
        for (String key : frequency.keySet()) {
            int cnt = 0;
            for (int i = 0; i < ruleStructureResPOS.size(); i++) {
                Map<String, Integer> map = TextRankKeyWord.getWordList(ruleStructureResPOS.get(i).getText(), ruleStructureResPOS.get(i).getText());
                if (map.containsKey(key)) cnt++;
            }
            double idf = Math.log((double) ruleStructureResPOS.size() / (cnt + 1));
            weight.put(key, tf.get(key) * idf);
        }

        return weight;
    }

    public static double calSum(Map<String, Double> weight) {
        double sum = 0.0;
        for (String key : weight.keySet()) {
            sum += weight.get(key) * weight.get(key);
        }
        return Math.log(sum);
    }


    public static void main(String[] args) {
//        String content = "程序员(英文Programmer)是从事程序开发、维护的专业人员。一般将程序员分为程序设计人员和程序编码人员，但两者的界限并不非常清楚，特别是在中国。软件从业人员分为初级程序员、高级程序员、系统分析员和项目经理四大类。";
        String content = "根据整体发展战略，确定风险偏好，制定风险管理政策。风险管理政策应与本行的发展规划、资本实力、经营目标和风险管理能力相适应，并符合法律法规和监管要求。";
        System.out.println(TextRankKeyWord.getWordList("", content));

    }
}
