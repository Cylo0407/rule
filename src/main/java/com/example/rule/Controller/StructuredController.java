package com.example.rule.Controller;


import com.example.rule.Service.StructuredService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/struct")
public class StructuredController {
    @Autowired
    StructuredService structuredService;

    @PostMapping("/rule")
    public boolean structureRules() {
        return false;
    }
}
