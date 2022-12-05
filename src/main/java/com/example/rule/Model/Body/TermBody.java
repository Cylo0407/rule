package com.example.rule.Model.Body;

import com.hankcs.hanlp.seg.common.Term;

import java.io.Serializable;

public class TermBody implements Serializable {
    private String word;
    private String nature;
    private Integer freq;
    private Double tf;
    private Double df;
    private Double idf;
    private Double tfidf;

    public TermBody(Term term) {
        this.setWord(term.word);
        this.setNature(term.nature.toString());
        this.setFreq(0);
        this.setTf(0.0);
        this.setDf(0.0);
        this.setIdf(0.0);
        this.setTfidf(0.0);
    }

    public TermBody(String word, String nature) {
        this.setWord(word);
        this.setNature(nature);
        this.setFreq(0);
        this.setTf(0.0);
        this.setDf(0.0);
        this.setIdf(0.0);
        this.setTfidf(0.0);
    }

    public TermBody(String word, String nature, Integer freq) {
        this.setWord(word);
        this.setNature(nature);
        this.setFreq(freq);
        this.setTf(0.0);
        this.setDf(0.0);
        this.setIdf(0.0);
        this.setTfidf(0.0);
    }

    public String toDictionaryLine() {
        return this.getWord() + " " + this.getNature() + " " + this.getFreq();
    }

    public boolean nameEqual(TermBody t) {
        return this.getWord().equals(t.getWord());
    }

    @Override
    public String toString() {
        return this.getWord() + "/" + this.getNature() + "=" + this.getFreq();
    }

    public Double getTf() {
        return tf;
    }

    public void setTf(Double tf) {
        this.tf = tf;
    }

    public Double getDf() {
        return df;
    }

    public void setDf(Double df) {
        this.df = df;
    }

    public Double getIdf() {
        return idf;
    }

    public void setIdf(Double idf) {
        this.idf = idf;
    }

    public Double getTfidf() {
        return tfidf;
    }

    public void setTfidf(Double tfidf) {
        this.tfidf = tfidf;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getNature() {
        return nature;
    }

    public void setNature(String nature) {
        this.nature = nature;
    }

    public Integer getFreq() {
        return freq;
    }

    public void setFreq(Integer freq) {
        this.freq = freq;
    }
}
