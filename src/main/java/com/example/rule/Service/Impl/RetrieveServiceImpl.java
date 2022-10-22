package com.example.rule.Service.Impl;

import com.example.rule.Service.RetrieveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
public class RetrieveServiceImpl implements RetrieveService {

    @Override
    public boolean retrieve() {
        return true;
    }

}
