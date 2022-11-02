package com.example.rule.Model.VO;

import org.apache.commons.lang3.tuple.Pair;
import lombok.Data;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

@Data
public class MatchResVO {
    String input_title;
    String input_text;
    List<Triple<Double, Integer, Pair<String, String>>> ruleMatchRes;
}
