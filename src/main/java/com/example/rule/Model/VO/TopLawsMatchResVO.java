package com.example.rule.Model.VO;

import com.example.rule.Model.PO.TopLawsOfRulePO;
import lombok.Data;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Set;

@Data
public class TopLawsMatchResVO {
    String input_title;
    //    String input_text;
    String topLaws;
    //    List<Triple<Double, Integer, Triple<String, String, String>>> ruleMatchRes;
    Set<TopLawsOfRulePO> ruleMatchRes;
}
