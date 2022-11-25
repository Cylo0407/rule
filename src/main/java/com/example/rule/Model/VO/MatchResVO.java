package com.example.rule.Model.VO;

import com.example.rule.Model.Body.MatchesBody;
import lombok.Data;

import java.util.List;

@Data
public class MatchResVO {
    String input_fileName;
    String input_text;
    List<MatchesBody> ruleMatchRes;
}
