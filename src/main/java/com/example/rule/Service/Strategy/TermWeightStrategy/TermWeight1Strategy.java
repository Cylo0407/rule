package com.example.rule.Service.Strategy.TermWeightStrategy;

import com.example.rule.Model.Body.TermBody;
import com.example.rule.Util.TermProcessingUtil;

import java.util.List;

public class TermWeight1Strategy implements TermWeightStrategy {

    @Override
    public List<TermBody> calTermFreq(String context) {
        return TermProcessingUtil.calTermFreq(context, 1);
    }
}
