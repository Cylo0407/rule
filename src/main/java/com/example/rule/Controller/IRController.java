package com.example.rule.Controller;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/ir")
public class IRController {


    @PostMapping("/retrieval")
    public boolean retrieveRules() {
        return true;
    }
}
