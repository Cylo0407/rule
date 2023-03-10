package com.example.rule.Controller;

import com.example.rule.Service.MeasureService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/measure")
public class MeasureController {

    @Resource
    MeasureService measureService;

    @GetMapping()
    public void doMeasure() {
        measureService.doMeasure();
    }

    @GetMapping("/reGenerateItem")
    public void reGenerateItemThenMeasure() {
        measureService.reGenerateItemThenMeasure();
    }

    @GetMapping("/reGenerateChapter")
    public void reGenerateChapterThenMeasure() {
        measureService.reGenerateChapterThenMeasure();
    }

}
