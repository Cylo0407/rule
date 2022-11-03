package com.example.rule.Model.VO;

import lombok.Data;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

@Data
public class TopLawsMatchResVO {
    String input_title;
    String input_text;
    String topLaws;
    List<Triple<Double, Integer, Triple<String, String, String>>> ruleMatchRes;
}
