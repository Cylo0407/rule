package com.example.rule.Model.IRModel;

import com.example.rule.Model.PO.RuleStructureResPO;
import com.example.rule.Util.TermProcessingUtil;

import java.util.*;

public class VSM implements IR_Model {

    @Override
    public Map<String, Double> calTermsWeight(Map<String, Integer> frequency, Map<RuleStructureResPO, Map<String, Integer>> frequencyOfRules) {
        return TermProcessingUtil.calTFIDF(frequency, frequencyOfRules);
    }
}
