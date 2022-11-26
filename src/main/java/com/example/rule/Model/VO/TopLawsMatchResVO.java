package com.example.rule.Model.VO;

import com.example.rule.Model.PO.TopLaws.TopLawsOfRulePO;
import lombok.Data;

import java.util.Set;

@Data
public class TopLawsMatchResVO {
    String input_title;
    //    String input_text;
    String topLaws;
    //    List<Triple<Double, Integer, Triple<String, String, String>>> ruleMatchRes;
    Set<TopLawsOfRulePO> ruleMatchRes;
}
