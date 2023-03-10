package com.example.rule.Util;

import com.example.rule.Model.Body.MatchesBody;
import com.example.rule.Model.PO.RuleStructureRes.RuleStructureResPO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConversionUtil {
    public static List<MatchesBody> similarityToResult(Map<Integer, Double> sims, List<? extends RuleStructureResPO> resPOS) {
        List<MatchesBody> res = new ArrayList<>();
        for (RuleStructureResPO po : resPOS) {
            MatchesBody ruleResMatch = po.toMatchesBody(sims);
            res.add(ruleResMatch);
        }
        return res;
    }
}
