package com.example.rule.Util;

import com.example.rule.Model.Config.PathConfig;
import com.example.rule.Model.IRModel.IR_Model;
import com.example.rule.Model.PO.RuleStructureResPO;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary;
import com.hankcs.hanlp.seg.common.Term;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 词处理工具
 */
public class TermProcessingUtil {
    /**
     * 计算词频
     *
     * @param content 文本
     * @return 词频列表
     */
    public static List<String> countTermFreq(String content) {
        List<Term> termList = HanLP.segment(content.replace("\n", "，"));
        List<String> wordList = new ArrayList<>();
        for (Term t : termList) {
            if (shouldInclude(t)) {
                wordList.add(t.word);
            }
        }
        return wordList;
    }

    /**
     * 计算文本的词频
     *
     * @param content 文本内容
     * @return 词频映射表
     */
    public static Map<String, Integer> calTermFreq(String content) {
        List<String> wordList = TermProcessingUtil.countTermFreq(content);
        // 计算每个词的词频
        Map<String, Integer> frequency = new HashMap<>();
        for (String word : wordList) {
            frequency.put(word, frequency.getOrDefault(word, 0) + 1);
        }
        return frequency;
    }

    /**
     * 是否应当将这个term纳入计算，词性属于名词、动词、词组
     *
     * @param term Term对象
     * @return 是否应当纳入
     */
    public static boolean shouldInclude(Term term) {
        return CoreStopWordDictionary.shouldInclude(term);
//        String termPos = term.nature.toString();
//        boolean isTermPos = (termPos.startsWith("n") && (!termPos.equals("nr")))
//                || termPos.equals("v")
//                || termPos.equals("vn")
//                || termPos.equals("phr");
//        boolean isTermLen = term.word.length() > 1;
//        return !(isTermPos && isTermLen);
    }

    /**
     * 计算TF
     *
     * @param frequency 词频
     * @return TF矩阵
     */
    public static Map<String, Double> calTFs(Map<String, Integer> frequency) {
        // 计算一条内规的总词数
        int termsNum = 0;
        for (String termName : frequency.keySet()) {
            termsNum += frequency.get(termName);
        }

        // 计算TF：词频/单篇文章（语料段落）中的所有词数量总和
        Map<String, Double> tf = new HashMap<>();
        for (String key : frequency.keySet()) {
            tf.put(key, (double) frequency.get(key) / termsNum);
        }
        return tf;
    }

    public static int calDF(String term, Map<RuleStructureResPO, Map<String, Integer>> frequencyOfRules) {
        int df = 0;
        // 对内规库的一条语料计算DF
        for (Map.Entry<RuleStructureResPO, Map<String, Integer>> me : frequencyOfRules.entrySet()) {
            Map<String, Integer> map = me.getValue();
            if (map.containsKey(term)) df++;
        }
        return df;
    }

    public static Map<String, Double> calIDFs(Map<String, Integer> frequency, Map<RuleStructureResPO, Map<String, Integer>> frequencyOfRules) {
        // 计算IDF：先计算DF，即一个词在所有文章中出现的次数（每有一篇文章/一段语料中出现记为1，再取倒数
        Map<String, Double> termsIDF = new HashMap<>();
        for (String term : frequency.keySet()) {
            int df = TermProcessingUtil.calDF(term, frequencyOfRules);
            double idf = 0.0;
            if (df > 0) {
                idf = Math.log((double) frequencyOfRules.size() / df);
            }
            termsIDF.put(term, idf);
        }
        return termsIDF;
    }

    /**
     * 计算一条内规的TF-IDF值
     *
     * @param frequency        用一条内规（内规标题+内规内容）构建出的词频映射表
     * @param frequencyOfRules 内规库中每个内规的词频集合
     * @return term-TFIDF映射
     */
    public static Map<String, Double> calTFIDF(Map<String, Integer> frequency, Map<RuleStructureResPO, Map<String, Integer>> frequencyOfRules) {
        Map<String, Double> termsTF = TermProcessingUtil.calTFs(frequency);
        Map<String, Double> termsIDF = TermProcessingUtil.calIDFs(frequency, frequencyOfRules);
        Map<String, Double> termsTFIDF = new HashMap<>();
        for (String term : termsTF.keySet()) {
            termsTFIDF.put(term, termsTF.get(term) * termsIDF.get(term));
        }
        return termsTFIDF;
    }

    public static Map<RuleStructureResPO, Map<String, Integer>> generateTermsFreq(List<RuleStructureResPO> ruleStructureResPOS) throws IOException, ClassNotFoundException {
        Map<RuleStructureResPO, Map<String, Integer>> frequencyOfRules = new HashMap<>();
        File rulesWordsFrequency = new File(PathConfig.termsInfoCache + File.separator + PathConfig.termsFrequencyCache);
        if (rulesWordsFrequency.exists()) {
            // 如果有缓存则直接读缓存
            frequencyOfRules = (Map<RuleStructureResPO, Map<String, Integer>>) IOUtil.readObject(rulesWordsFrequency);
        } else {
            // 对内规库里检索到的每条内规执行如下：
            for (RuleStructureResPO ruleStructureResPO : ruleStructureResPOS) {
                // 获取一条内规的词频
                Map<String, Integer> ruleFrequency = TermProcessingUtil.calTermFreq(ruleStructureResPO.getTitle() + ruleStructureResPO.getText());
                // 存储内规词频
                frequencyOfRules.put(ruleStructureResPO, ruleFrequency);
            }
            rulesWordsFrequency.createNewFile();
            IOUtil.writeObject(rulesWordsFrequency, frequencyOfRules);
        }
        return frequencyOfRules;
    }

    public static Map<RuleStructureResPO, Map<String, Double>> generateTermsTFIDF(List<RuleStructureResPO> ruleStructureResPOS, Map<RuleStructureResPO, Map<String, Integer>> frequencyOfRules, IR_Model model) throws IOException, ClassNotFoundException {
        Map<RuleStructureResPO, Map<String, Double>> tfidfOfRules = new HashMap<>();
        File rulesWordsTFIDF = new File(PathConfig.termsInfoCache + File.separator + PathConfig.termsTFIDFCache);
        if (rulesWordsTFIDF.exists()) {
            tfidfOfRules = (Map<RuleStructureResPO, Map<String, Double>>) IOUtil.readObject(rulesWordsTFIDF);
        } else {
            for (RuleStructureResPO ruleStructureResPO : ruleStructureResPOS) {
                // 计算一条内规的TF-IDF
                Map<String, Double> tfidfOfRule = model.calTermsWeight(frequencyOfRules.get(ruleStructureResPO), frequencyOfRules);
                tfidfOfRules.put(ruleStructureResPO, tfidfOfRule);
            }
            rulesWordsTFIDF.createNewFile();
            IOUtil.writeObject(rulesWordsTFIDF, tfidfOfRules);
        }
        return tfidfOfRules;
    }

    public static List<Pair<RuleStructureResPO, Double>> calSimilarity(Map<String, Double> tfidfOfInput, Map<RuleStructureResPO, Map<String, Double>> tfidfOfRules) {
        List<Pair<RuleStructureResPO, Double>> similarityBetweenInputAndRules = new ArrayList<>();

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
        return similarityBetweenInputAndRules;
    }

    public static void main(String[] args) {
//        String content = "程序员(英文Programmer)是从事程序开发、维护的专业人员。一般将程序员分为程序设计人员和程序编码人员，但两者的界限并不非常清楚，特别是在中国。软件从业人员分为初级程序员、高级程序员、系统分析员和项目经理四大类。";
        String content = "根据整体发展战略，确定风险偏好，制定风险管理政策。风险管理政策应与本行的发展规划、资本实力、经营目标和风险管理能力相适应，并符合法律法规和监管要求。";
        System.out.println(TermProcessingUtil.calTermFreq(content));

    }
}
