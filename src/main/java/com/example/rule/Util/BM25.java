package com.example.rule.Util;

import com.example.rule.Model.PO.RuleStructureResPO;

import java.util.HashMap;
import java.util.Map;

public class BM25 {

    /**
     * 调节因子
     */
    final static float k1 = 1.5f;

    /**
     * 调节因子
     */
    final static float b = 0.75f;

    public BM25() {
    }

    public static Map<String, Double> getKeyWords(Map<String, Integer> frequency, Map<RuleStructureResPO, Map<String, Integer>> frequencyOfRules) {
//        计算一条内规的总词数
        int termsNum = 0;
        for (String termName : frequency.keySet()) {
            termsNum += frequency.get(termName);
        }

        // 计算TF：词频/单篇文章（语料段落）中的所有词数量总和
        Map<String, Double> tf = new HashMap<>();
        for (String key : frequency.keySet()) {
            tf.put(key, (double) frequency.get(key) / termsNum);
        }

        int D = 0; //该篇文档长度
        double avglD = 0.0; //平均文档长度
        int N = frequencyOfRules.size(); //文档总数
        for (Map.Entry<String, Integer> me : frequency.entrySet()) {
            D += me.getValue();
        }
        for (Map.Entry<RuleStructureResPO, Map<String, Integer>> me : frequencyOfRules.entrySet()) {
            Map<String, Integer> map = me.getValue();
            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                avglD += entry.getValue();
            }
        }
        avglD = avglD / N;

        // 计算IDF：
        Map<String, Double> weight = new HashMap<>();
        for (String key : frequency.keySet()) {
            int cnt = 0;
            // 对内规库的每一条语料再分词计算DF
            for (Map.Entry<RuleStructureResPO, Map<String, Integer>> me : frequencyOfRules.entrySet()) {
                Map<String, Integer> map = me.getValue();
                if (map.containsKey(key)) cnt++;
            }

            double idf = 0.0;
            if (cnt > 0) {
                idf = Math.log((double) ((N - cnt + 0.5) / (cnt + 0.5)) + 1);
            }

            double value = tf.get(key) * (k1 + 1) / (tf.get(key) + k1 * (1 - b + b * D / avglD));
            weight.put(key, value * idf);
        }

        return weight;
    }
}
