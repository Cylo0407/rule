package com.example.rule.Util;

import com.example.rule.Model.Body.TermBody;
import com.example.rule.Model.Config.NumberConfig;
import com.example.rule.Model.IRModel.IR_Model;
import com.example.rule.Model.PO.RuleStructureRes.RuleStructureResPO;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 词处理工具
 */
public class TermProcessingUtil {
    /**
     * 对文本进行分词
     *
     * @param context 待分词的文本
     * @return 分词列表
     */
    public static List<Term> segment(String context) {
        Segment segment = HanLP.newSegment().enableOrganizationRecognize(true);
        return segment.seg(context);
    }

    /**
     * 词汇清洗：去除分词结果中不需要的词语
     *
     * @param termList 分词列表
     * @return 清洗后的分词列表
     */
    private static List<Term> termCleanse(List<Term> termList) {
        termList.removeIf(TermProcessingUtil::shouldBeCleansed);
        return termList;
    }

    /**
     * 清洗文本：把文本中不需要的内容清除
     * 去除所有空格；将网址全部替换为空格；将所有前缀信息替换为空格；再将所有空白字符全部缩减为一个
     *
     * @param context 源文本
     * @return 清洗后的文本
     */
    public static String cleanseContext(String context) {
        // 去除所有网址
        return context.replaceAll(" ", "")
                .replaceAll("http[^\u4E00-\u9FA5]+", " ")
                .replaceAll("[a-z]+:[0-9]+", "")
                .replaceAll("[^0-9a-zA-Z\u4E00-\u9FA5，。、；：%]", " ")
                .replaceAll("\\s+", " ");
    }


    /**
     * 计算文本的词频，默认的长词词频为3
     *
     * @param context 文本内容
     * @return 词频映射表
     */

    public static List<TermBody> calTermFreq(String context) {
        List<Term> cleansedTermList = TermProcessingUtil.preprocessContextToTermList(context);
        // 计算每个词的词频
        return TermProcessingUtil.countTerms(cleansedTermList);
    }

    /**
     * 计算词频：计算每一个term出现的次数
     * 名词的权重更高
     * 通常来说名词的长度越长，证明这个词语越专业化：我们认为长词的词频为3，短词的词频为1
     * 如果该名词是相关法案natk，我们认为其词频为4
     * TODO 在此优化新的词频策略
     *
     * @param termList 清洗后的分词列表
     * @return 词频列表
     */
    private static List<TermBody> countTerms(List<Term> termList) {
        Map<String, TermBody> termsFrequency = new HashMap<>();
        for (Term t : termList) {
            TermBody tFreq = termsFrequency.getOrDefault(t.word, new TermBody(t));
            if (tFreq.getWord().length() > 2) {
                tFreq.setFreq(tFreq.getFreq() + NumberConfig.longTermWeight);
            } else {
                tFreq.setFreq(tFreq.getFreq() + 1);
            }
            termsFrequency.put(t.word, tFreq);
        }
        ArrayList<TermBody> termBodyList = new ArrayList<>(termsFrequency.values());
//         计算一条内规的总词数
//        int termsNum = 0;
//        for (TermBody termBody : termBodyList) {
//            termsNum += termBody.getFreq();
//        }
//        for (TermBody tq : termBodyList) {
//            if (tq.getNature().equals("natk")) {
//                tq.setFreq(tq.getFreq() + (int) Math.round(termsNum * 0.01));
//                tq.setFreq(tq.getFreq() + 10);
//            }
//        }
        termBodyList.sort(Comparator.comparingInt(o -> -o.getFreq()));
        return termBodyList;
    }

    /**
     * 将原始文本处理为TermList
     *
     * @param context 原始文本
     * @return termList
     */
    public static List<Term> preprocessContextToTermList(String context) {
        context = TermProcessingUtil.cleanseContext(context);
        List<Term> termList = TermProcessingUtil.segment(context);
        return TermProcessingUtil.termCleanse(termList);
    }


    /**
     * 是否应当将这个term纳入计算，词性属于名词、动词、词组
     * 排除长度<2的词基本验证是负优化
     *
     * @param term Term对象
     * @return 是否应当纳入
     */
    public static boolean shouldBeCleansed(Term term) {
        String termPos = term.nature.toString();
//        boolean isTermPos = (termPos.startsWith("n") && (!termPos.equals("nr")))
//                || termPos.equals("v")
//                || termPos.equals("vn")
//                || termPos.equals("phr");
        boolean isTermPos = CoreStopWordDictionary.shouldInclude(term);
        return !isTermPos;
    }


    /**
     * 计算TF
     *
     * @param inputTermsBodies 词频对象列表
     */
    public static void calTF(List<TermBody> inputTermsBodies) {
        // 计算一条内规的总词数
        int termsNum = 0;
        for (TermBody termBody : inputTermsBodies) {
            termsNum += termBody.getFreq();
        }

        // 计算TF：词频/单篇文章（语料段落）中的所有词数量总和
        for (TermBody termBody : inputTermsBodies) {
            termBody.setTf((double) termBody.getFreq() / termsNum);
        }
    }

    public static void calDF(List<TermBody> inputTermsBodies, ArrayList<TermBody> corpusTermBodies) {
        // 对内规库的一条语料计算DF
        for (TermBody inputTermBody : inputTermsBodies) {
            for (TermBody corpusTermBody : corpusTermBodies) {
                if (inputTermBody.nameEqual(corpusTermBody)) {
                    inputTermBody.setDf(inputTermBody.getDf() + 1);
                }
            }
        }
    }

    public static void calIDF(List<TermBody> inputTermsBodies, Collection<List<TermBody>> rulesTermsBodies) {
        // 计算IDF：先计算DF，即一个词在所有文章中出现的次数（每有一篇文章/一段语料中出现记为1，再取倒数
        // 获取语料库中所有词实体
        ArrayList<TermBody> corpusTermBodies = new ArrayList<>();
        for (List<TermBody> termBody : rulesTermsBodies) {
            corpusTermBodies.addAll(termBody);
        }
        TermProcessingUtil.calDF(inputTermsBodies, corpusTermBodies);
        for (TermBody inputTermBody : inputTermsBodies) {
            if (inputTermBody.getDf() > 0) {
                inputTermBody.setIdf(Math.log((double) rulesTermsBodies.size() / inputTermBody.getDf()));
            }
        }
    }

    /**
     * 计算一条内规的TF-IDF值
     *
     * @param inputTermsBodies 外部输入的词频列表
     * @param rulesTermsBodies 内规库中每个内规的词频对象映射
     *                         term-TFIDF映射
     */
    public static void calTFIDF(List<TermBody> inputTermsBodies, Map<Integer, List<TermBody>> rulesTermsBodies) {
        TermProcessingUtil.calTF(inputTermsBodies);
        TermProcessingUtil.calIDF(inputTermsBodies, rulesTermsBodies.values());
        for (List<TermBody> ruleTermBodies : rulesTermsBodies.values()) {
            for (TermBody ruleTermBody : ruleTermBodies) {
                ruleTermBody.setTfidf(ruleTermBody.getTf() * ruleTermBody.getIdf());
            }
        }
    }

    /**
     * @param resPOS   内规实体列表
     * @param pathName 缓存地址
     * @return 词频实体映射
     */
    public static Map<Integer, List<TermBody>> generateTermsFreq(List<? extends RuleStructureResPO> resPOS, String pathName) throws IOException, ClassNotFoundException {
        Map<Integer, List<TermBody>> frequencyOfRules = new HashMap<>();
        File rulesWordsFrequency = new File(pathName);
        if (rulesWordsFrequency.exists()) {
            // 如果有缓存则直接读缓存
            frequencyOfRules = (Map<Integer, List<TermBody>>) IOUtil.readObject(rulesWordsFrequency);
        } else {
            for (RuleStructureResPO resPO : resPOS) {
                frequencyOfRules.put(resPO.getId(), resPO.toTermsFreq());
            }
        }
        rulesWordsFrequency.createNewFile();
        IOUtil.writeObject(rulesWordsFrequency, frequencyOfRules);
        return frequencyOfRules;
    }

    /**
     * 生成所有内规的tf-idf
     *
     * @param frequencyOfRules 所有内规的词频列表Map
     * @param pathName         缓存地址
     * @param model            计算模型
     */
    public static Map<Integer, List<TermBody>> generateTermsTFIDF(Map<Integer, List<TermBody>> frequencyOfRules, String pathName, IR_Model model) throws IOException, ClassNotFoundException {
        Map<Integer, List<TermBody>> tfidfOfRules;
        File rulesWordsTFIDF = new File(pathName);
        if (rulesWordsTFIDF.exists()) {
            tfidfOfRules = (Map<Integer, List<TermBody>>) IOUtil.readObject(rulesWordsTFIDF);
        } else {
            // 对每个内规的词频值，计算其权重
            for (List<TermBody> termBodies : frequencyOfRules.values()) {
                model.calTermsWeight(termBodies, frequencyOfRules);
            }
            tfidfOfRules = frequencyOfRules;
            rulesWordsTFIDF.createNewFile();
            IOUtil.writeObject(rulesWordsTFIDF, tfidfOfRules);
        }
        return tfidfOfRules;
    }

    public static Map<Integer, Double> calSimilarity(List<TermBody> tfidfOfInput, Map<Integer, List<TermBody>> tfidfOfRules) {
        Map<Integer, Double> similarityBetweenInputAndRules = new HashMap<>();

        for (Integer id : tfidfOfRules.keySet()) {
            List<TermBody> termBodies = tfidfOfRules.get(id);
            // 计算向量模
            double a = 0.0;
            for (TermBody t : tfidfOfInput) {
                a += t.getTfidf() * t.getTfidf();
            }
            a = Math.sqrt(a);

            double b = 0.0;
            for (TermBody t : termBodies) {
                b += t.getTfidf() * t.getTfidf();
            }
            b = Math.sqrt(b);

            // 计算向量点积
            double ab = 0.0;
            for (TermBody t : tfidfOfInput) {
                for (TermBody tb : termBodies) {
                    if (t.nameEqual(tb)) {
                        ab += t.getTfidf() * tb.getTfidf();
                    }
                }
            }
            double cos = ab / (a * b);
            similarityBetweenInputAndRules.put(id, cos);
        }
        return similarityBetweenInputAndRules;
    }

    public static void main(String[] args) {
//        String content = "程序员(英文Programmer)是从事程序开发、维护的专业人员。一般将程序员分为程序设计人员和程序编码人员，但两者的界限并不非常清楚，特别是在中国。软件从业人员分为初级程序员、高级程序员、系统分析员和项目经理四大类。";
        String content = "根据整体发展战略，确定风险偏好，制定风险管理政策。风险管理政策应与本行的发展规划、资本实力、经营目标和风险管理能力相适应，并符合法律法规和监管要求。";
        System.out.println(TermProcessingUtil.calTermFreq(content));

    }
}
