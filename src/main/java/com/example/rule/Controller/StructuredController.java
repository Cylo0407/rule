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
@RequestMapping("/rule")
public class StructuredController {
    @Resource
    StructuredService structuredService;

    @PostMapping("/structure")
    public boolean structureRules() {
        String filePath = "/Users/cyl/rule/src/File/标准版内规/运营管理部--制度";
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
}
