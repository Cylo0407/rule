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

    /**
     * 计算文本的词频（其实可以把标题和内容合并为一个参数）
     *
     * @param title   文本标题
     * @param content 文本内容
     * @return 词频映射表
     */
    public static Map<String, Integer> getWordList(String title, String content) {
        List<Term> termList = HanLP.segment(title + content);
        List<String> wordList = new ArrayList<String>();
        for (Term t : termList) {
            if (shouldInclude(t)) {
                wordList.add(t.word);
            }
        }

        // 计算每个词的词频
        Map<String, Integer> frequency = new HashMap<>();
        for (String word : wordList) {
            frequency.put(word, frequency.getOrDefault(word, 0) + 1);
        }
        return frequency;
    }

    /**
     * 是否应当将这个term纳入计算，词性属于名词、动词、副词、形容词
     * TODO 停用词表后续还可以优化
     *
     * @param term
     * @return 是否应当
     */
    public static boolean shouldInclude(Term term) {
        return CoreStopWordDictionary.shouldInclude(term);
    }


    /**
     * 计算一条内规的TF-IDF值
     *
     * @param frequency           用一条内规（内规标题+内规内容）构建出的词频映射表
     * @param ruleStructureResPOS 内规库的结构化内容
     * @return
     */
    public static Map<String, Double> getKeyWords(Map<String, Integer> frequency, List<RuleStructureResPO> ruleStructureResPOS) {
//        计算词频最高的词
//        int maxFreq = 0;
//        for (String key : frequency.keySet()) {
//            if (frequency.get(key) > maxFreq) maxFreq = frequency.get(key);
//        }
//        计算一条内规的总词数
        int termsNum = 0;
        for (String termName : frequency.keySet()) {
            termsNum += frequency.get(termName);
        }

        // 计算TF：词频/单篇文章（语料段落）中的所有词数量总和
        Map<String, Double> tf = new HashMap<>();
        for (String key : frequency.keySet()) {
//            tf.put(key, (double) frequency.get(key) / maxFreq);
            tf.put(key, (double) (frequency.get(key) / termsNum));
        }

        // 计算IDF：先计算DF，即一个词在所有文章中出现的次数（每有一篇文章/一段语料中出现记为1），再取倒数
        Map<String, Double> weight = new HashMap<>();
        for (String key : frequency.keySet()) {
            int cnt = 0;
            // 对内规库的每一条语料再分词计算DF
            for (int i = 0; i < ruleStructureResPOS.size(); i++) {
                // TODO 如果把语料库构建好的结果保存下来就可以优化掉这里
                Map<String, Integer> map = TextRankKeyWord.getWordList(ruleStructureResPOS.get(i).getTitle(), ruleStructureResPOS.get(i).getText());
//                Map<String, Integer> map = TextRankKeyWord.getWordList(ruleStructureResPOS.get(i).getText(), ruleStructureResPOS.get(i).getText());
                if (map.containsKey(key)) cnt++;
            }
//            double idf = Math.log((double) ruleStructureResPOS.size() / (cnt + 1));

            // cnt为0则idf视为0
            double idf = 0.0;
            if (cnt > 0) {
                idf = (double) ruleStructureResPOS.size()/cnt;
            }
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
