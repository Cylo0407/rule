package com.example.rule.Service.Strategy.TermWeightStrategy;

import com.example.rule.Model.Body.TermBody;

import java.util.List;

public interface TermWeightStrategy {
    List<TermBody> calTermFreq(String context);
}
