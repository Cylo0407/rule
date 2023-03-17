package com.example.rule.Controller;


import com.example.rule.Model.Config.PathConfig;
import com.example.rule.Service.StructuredService;
import com.example.rule.Util.IOUtil;
import org.springframework.web.bind.annotation.PathVariable;
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
        structuredService.structureRules();
        return true;
    }

    @PostMapping("/rule/{granularity}")
    public boolean structureRulesWithGranularity(@PathVariable String granularity) {
        structuredService.structureRulesWithGranularity(granularity);
        return true;
    }

    @PostMapping("/input/Interpretation")
    public boolean interpretationStructureInputs() {
        structuredService.preDealInterpretationContents(PathConfig.interpretationInputPath, -1);
        return true;
    }

    @PostMapping("/input/SourceDoc")
    public boolean sourceDocStructureInputs() {
        structuredService.preDealInterpretationContents(PathConfig.sourceDocInputPath, -1);
        return true;
    }


}
