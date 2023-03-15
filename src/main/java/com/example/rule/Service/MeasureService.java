package com.example.rule.Service;

public interface MeasureService {
    void doMeasure(String granularity);
    void reGenerateItemThenMeasure();
    void reGenerateChapterThenMeasure();
}
