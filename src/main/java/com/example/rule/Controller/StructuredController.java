package com.example.rule.Controller;


import com.example.rule.Service.StructuredService;
import com.example.rule.Util.DocReadUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;


@RestController
@RequestMapping("/structure")
public class StructuredController {
    @Resource
    StructuredService structuredService;

    @PostMapping("/rule")
    public boolean structureRules() {
//        String filePath = "/Users/cyl/Downloads/标准版内规/信贷管理部--制度";
//        String filePath = "/Users/cyl/Downloads/标准版内规/运营管理部--制度";
        String filePath = "/Users/cyl/Downloads/标准版内规/信贷";
        File dir = new File(filePath);
        File[] fs = dir.listFiles();
        for (File file : fs) {
            String fileName = file.getName();
            if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
                System.out.println(fileName);
                List<String> texts = DocReadUtils.readWord(file);
                structuredService.structureRules(texts);
            }
        }
        return true;
    }

    @PostMapping("/input")
    public boolean structureInputs() {
        structuredService.preDealPenaltyCaseContents("/Users/cyl/Downloads/第4组迭代一/caseBase数据库/punishment.csv", 10);
        structuredService.preDealInterpretationContents("/Users/cyl/Downloads/data/Interpretation", 10);

        return true;
    }
}
