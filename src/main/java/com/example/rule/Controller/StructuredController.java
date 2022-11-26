package com.example.rule.Controller;


import com.example.rule.Service.StructuredService;
import com.example.rule.Util.IOUtil;
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
//        String filePath = "F:\\魔鬼的力量\\_A研究生资料\\面向互联网+助教材料\\标准版内规";
        String filePath = "F:\\DataSet\\银行内规项目数据集\\匹配内规候选集合";
        File dir = new File(filePath);
        File[] fs = dir.listFiles();
        for (File f : fs) {
//            System.out.println("dirName: " + f.getName());
            if (f.getName().equals(".DS_Store")) continue;

//            File[] rulesPart = f.listFiles();
//            for (File file : rulesPart) {
            String fileName = f.getName();
            if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
                System.out.println(fileName);
                List<String> texts = IOUtil.readWord(f);
                structuredService.structureRules(texts, fileName.substring(0, fileName.lastIndexOf(".")));
            }
//            }
        }
        return true;
    }

    @PostMapping("/input")
    public boolean structureInputs() {
//        structuredService.preDealPenaltyCaseContents("F:\\魔鬼的力量\\_A研究生资料\\面向互联网+助教材料\\外部数据\\第4组迭代一\\第四组casebase最新数据\\caseBase数据库\\punishment.csv", 10);
        structuredService.preDealInterpretationContents("F:\\DataSet\\银行内规项目数据集\\外规输入候选集合", 4);
        return true;
    }


}
