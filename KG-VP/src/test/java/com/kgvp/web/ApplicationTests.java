package com.kgvp.web;

import com.kgvp.web.service.KGGraphService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ApplicationTests {

    @Autowired
    private KGGraphService kgGraphService;

    @Test
    void contextLoads() {
    }
}
