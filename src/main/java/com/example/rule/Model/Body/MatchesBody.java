package com.example.rule.Model.Body;

public class MatchesBody {
    private Double similarity;
    private Integer rule_id;
    private String rule_fileName;
    private String rule_text;
    private Integer relevance;

    public MatchesBody(Double similarity, Integer rule_id, String rule_fileName, String rule_text, Integer relevance) {
        this.similarity = similarity;
        this.rule_id = rule_id;
        this.rule_fileName = rule_fileName;
        this.rule_text = rule_text;
        this.relevance = relevance;
    }

    public Double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Double similarity) {
        this.similarity = similarity;
    }

    public Integer getRule_id() {
        return rule_id;
    }

    public void setRule_id(Integer rule_id) {
        this.rule_id = rule_id;
    }

    public String getRule_fileName() {
        return rule_fileName;
    }

    public void setRule_fileName(String rule_fileName) {
        this.rule_fileName = rule_fileName;
    }

    public String getRule_text() {
        return rule_text;
    }

    public void setRule_text(String rule_text) {
        this.rule_text = rule_text;
    }

    public Integer getRelevance() {
        return relevance;
    }

    public void setRelevance(Integer relevance) {
        this.relevance = relevance;
    }
}
