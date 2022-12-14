package com.example.rule.Util;

import com.example.rule.Model.Body.TermBody;
import com.example.rule.Model.Config.PathConfig;
import com.example.rule.Model.IRModel.IR_Model;
import com.example.rule.Model.PO.RuleArticleStructureResPO;
import com.example.rule.Model.PO.RuleChapterStructureResPO;
import com.example.rule.Model.PO.RuleStructureResPO;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.stopword.CoreStopWordDictionary;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import org.apache.poi.ss.formula.functions.T;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
     * 计算文本的词频
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
     * 通常来说名词的长度越长，证明这个词语越专业化：我们认为长词的词频为2，短词的词频为1
     * 如果该名词是相关法案natk，我们认为其词频为4
     *
     * @param termList 清洗后的分词列表
     * @return 词频列表
     */
    private static List<TermBody> countTerms(List<Term> termList) {
        Map<String, TermBody> termsFrequency = new HashMap<>();
        for (Term t : termList) {
            TermBody tFreq = termsFrequency.getOrDefault(t.word, new TermBody(t));
            if (tFreq.getWord().length() > 2) {
                tFreq.setFreq(tFreq.getFreq() + 2);
            } else {
                tFreq.setFreq(tFreq.getFreq() + 1);
            }
            termsFrequency.put(t.word, tFreq);
        }
        ArrayList<TermBody> termBodyList = new ArrayList<>(termsFrequency.values());
        for (TermBody tq : termBodyList) {
            if (tq.getNature().equals("natk")) {
                tq.setFreq(tq.getFreq() + 3);
            }
        }
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
        boolean isTermLen = term.word.length() > 1;
        return !(isTermPos && isTermLen);
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

    public static void calDF(List<TermBody> inputTermsBodies, ArrayList<TermBody> allTermBodies) {
        // 对内规库的一条语料计算DF
        for (TermBody termBody : inputTermsBodies) {
            for (TermBody t : allTermBodies) {
                if (termBody.nameEqual(t)) {
                    termBody.setDf(termBody.getDf() + 1);
                }
            }
        }
    }

    public static void calIDF(List<TermBody> inputTermsBodies, Collection<List<TermBody>> rulesTermsBodies) {
        // 计算IDF：先计算DF，即一个词在所有文章中出现的次数（每有一篇文章/一段语料中出现记为1，再取倒数
        ArrayList<TermBody> allTermBodies = new ArrayList<>();
        for (List<TermBody> termBody : rulesTermsBodies) {
            allTermBodies.addAll(termBody);
        }
        TermProcessingUtil.calDF(inputTermsBodies, allTermBodies);
        for (TermBody termBody : inputTermsBodies) {
            if (termBody.getDf() > 0) {
                termBody.setIdf(Math.log((double) rulesTermsBodies.size() / termBody.getDf()));
            }
        }
    }

    /**
     * 计算一条内规的TF-IDF值
     *
     * @param rulesTermsBodies 内规库中每个内规的词频对象映射
     * @return term-TFIDF映射
     */
    public static void calTFIDF(List<TermBody> inputTermsBodies, Map<Integer, List<TermBody>> rulesTermsBodies) {
        TermProcessingUtil.calTF(inputTermsBodies);
        TermProcessingUtil.calIDF(inputTermsBodies, rulesTermsBodies.values());
        for (List<TermBody> termBodies : rulesTermsBodies.values()) {
            for (TermBody termBody : termBodies) {
                termBody.setTfidf(termBody.getTf() * termBody.getIdf());
            }
        }
    }

    public static Map<Integer, List<TermBody>> generateTermsFreq(List<?> resPOS) throws IOException, ClassNotFoundException {
        Map<Integer, List<TermBody>> frequencyOfRules = new HashMap<>();
        File rulesWordsFrequency = new File(PathConfig.termsInfoCache + File.separator + PathConfig.termsFrequencyCache);
        if (rulesWordsFrequency.exists()) {
            // 如果有缓存则直接读缓存
            frequencyOfRules = (Map<Integer, List<TermBody>>) IOUtil.readObject(rulesWordsFrequency);
        } else {
            for (Object resPO : resPOS) {
                if (resPO instanceof RuleStructureResPO) {
                    RuleStructureResPO po = (RuleStructureResPO) resPO;
                    frequencyOfRules.put(po.getId(), generateTermsFreqBySection(po));
                } else if (resPO instanceof RuleChapterStructureResPO) {
                    RuleChapterStructureResPO po = (RuleChapterStructureResPO) resPO;
                    frequencyOfRules.put(po.getId(), generateTermsFreqByChapter(po));
                } else if (resPO instanceof RuleArticleStructureResPO) {
                    RuleArticleStructureResPO po = (RuleArticleStructureResPO) resPO;
                    frequencyOfRules.put(po.getId(), generateTermsFreqByArticle(po));
                }
            }
        }
        rulesWordsFrequency.createNewFile();
        IOUtil.writeObject(rulesWordsFrequency, frequencyOfRules);
        return frequencyOfRules;
    }

    private static List<TermBody> generateTermsFreqBySection(RuleStructureResPO ruleStructureResPO) {
        return TermProcessingUtil.calTermFreq(ruleStructureResPO.getTitle() + ruleStructureResPO.getText());
    }

    //针对章节
    private static List<TermBody> generateTermsFreqByChapter(RuleChapterStructureResPO ruleChapterStructureResPO) {
        if (ruleChapterStructureResPO.getText() == null) {
            return new ArrayList<>();
        }
        return TermProcessingUtil.calTermFreq(ruleChapterStructureResPO.getTitle() + ruleChapterStructureResPO.getText());
    }

    private static List<TermBody> generateTermsFreqByArticle(RuleArticleStructureResPO ruleArticleStructureResPO) {
        if (ruleArticleStructureResPO.getText() == null) {
            return new ArrayList<>();
        }
        return TermProcessingUtil.calTermFreq(ruleArticleStructureResPO.getTitle() + ruleArticleStructureResPO.getText());
    }

    public static Map<Integer, List<TermBody>> generateTermsTFIDF(Map<Integer, List<TermBody>> frequencyOfRules, IR_Model model) throws IOException, ClassNotFoundException {
        Map<Integer, List<TermBody>> tfidfOfRules;
        File rulesWordsTFIDF = new File(PathConfig.termsInfoCache + File.separator + PathConfig.termsTFIDFCache);
        if (rulesWordsTFIDF.exists()) {
            tfidfOfRules = (Map<Integer, List<TermBody>>) IOUtil.readObject(rulesWordsTFIDF);
        } else {
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
