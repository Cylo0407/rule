package com.example.rule.Model.IRModel.Algorithm;

import com.example.rule.Model.Body.TermBody;
import com.example.rule.Model.IRModel.IR_Model;
import com.example.rule.Util.TermProcessingUtil;

import java.util.*;

public class BM25 implements IR_Model {

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

    @Override
    public void calTermsWeight(List<TermBody> inputTermsBodies, Map<Integer, List<TermBody>> rulesTermsBodies) {
        TermProcessingUtil.calTF(inputTermsBodies);

        int D = 0; //该篇文档长度
        double avglD = 0.0; //平均文档长度
        int N = rulesTermsBodies.size(); //文档总数
        for (TermBody termBody : inputTermsBodies) {
            D += termBody.getFreq();
        }
        // 把所有文章的所有词汇合
        ArrayList<TermBody> allTermBodies = new ArrayList<>();
        for (List<TermBody> termBody : rulesTermsBodies.values()) {
            allTermBodies.addAll(termBody);
        }
        for (TermBody t : allTermBodies) {
            avglD += t.getTf();
        }
        avglD = avglD / N;

        TermProcessingUtil.calDF(inputTermsBodies, allTermBodies);
        for (TermBody t : inputTermsBodies) {
            if (t.getDf() > 0) {
                t.setIdf(Math.log(((N - t.getDf() + 0.5) / (t.getDf() + 0.5)) + 1));
            }
            Double W = t.getTf() * (k1 + 1) / (t.getTf() + k1 * (1 - b + b * D / avglD));
            Double R = t.getIdf();
            t.setTfidf(W * R);
        }
    }
}
