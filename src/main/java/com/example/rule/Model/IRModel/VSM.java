package com.example.rule.Model.IRModel;

import com.example.rule.Model.Body.TermBody;
import com.example.rule.Model.PO.RuleStructureResPO;
import com.example.rule.Util.TermProcessingUtil;

import java.util.*;

public class VSM implements IR_Model {

    @Override
    public void calTermsWeight(List<TermBody> inputTermsBodies, Map<Integer, List<TermBody>> rulesTermsBodies) {
        TermProcessingUtil.calTFIDF(inputTermsBodies, rulesTermsBodies);
    }
}
