package com.example.rule.Model.IRModel;

import com.example.rule.Model.PO.RuleStructureResPO;

import java.util.Map;

public interface IR_Model {

    Map<String, Double> calTermsWeight(Map<String, Integer> frequency, Map<RuleStructureResPO, Map<String, Integer>> frequencyOfRules);
}
