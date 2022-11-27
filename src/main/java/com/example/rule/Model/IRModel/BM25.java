package com.example.rule.Model.IRModel;

import com.example.rule.Model.PO.RuleStructureResPO;
import com.example.rule.Util.TermProcessingUtil;

import java.util.*;

public class BM25 implements IR_Model{

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

    public Map<String, Double> calTermsWeight(Map<String, Integer> frequency, Map<RuleStructureResPO, Map<String, Integer>> frequencyOfRules) {
        Map<String, Double> termsTF = TermProcessingUtil.calTFs(frequency);

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

        Map<String, Double> termsWeight = new HashMap<>();
        for (String term : frequency.keySet()) {
            int df = TermProcessingUtil.calDF(term, frequencyOfRules);
            double idf = 0.0;
            if (df > 0) {
                idf = Math.log(((N - df + 0.5) / (df + 0.5)) + 1);
            }

            double value = termsTF.get(term) * (k1 + 1) / (termsTF.get(term) + k1 * (1 - b + b * D / avglD));
            termsWeight.put(term, value * idf);
        }

        return termsWeight;
    }
}
