package com.example.rule.Controller;


import com.example.rule.Model.Config.PathConfig;
import com.example.rule.Service.StructuredService;
import com.example.rule.Util.IOUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;
import java.util.Objects;


@RestController
@RequestMapping("/structure")
public class StructuredController {
    @Resource
    StructuredService structuredService;

    @PostMapping("/rule")
    public boolean structureRules() {
        String filePath = PathConfig.rulesPath;
        File dir = new File(filePath);
        structure(dir);
        return true;
    }

    private void structure(File rule) {
        if (rule.isDirectory()) {
            File[] fs = rule.listFiles();
            for (File f : Objects.requireNonNull(fs)) {
                structure(f);
            }
        } else {
            String fileName = rule.getName();
            if (rule.isFile() && (fileName.endsWith(".doc") || fileName.endsWith(".docx"))) {
                List<String> texts = IOUtil.readWordLines(rule);
                structuredService.structureRules(texts, fileName.substring(0, fileName.lastIndexOf(".")));
            }
        }
    }

    @PostMapping("/input")
    public boolean structureInputs() {
        structuredService.preDealInterpretationContents(PathConfig.inputPath, 441);
        return true;
    }


}
