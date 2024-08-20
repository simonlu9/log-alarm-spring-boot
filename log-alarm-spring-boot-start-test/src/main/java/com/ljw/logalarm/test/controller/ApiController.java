package com.ljw.logalarm.test.controller;


import com.ljw.logalarm.test.service.AsyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Slf4j
public class ApiController {
    @Autowired
    private AsyncService asyncService;
    @GetMapping("/greet")
    public ResponseEntity<String> greet() {
        log.info("greet");
        return ResponseEntity.ok("Hello, World!");
    }
    @GetMapping("/async")
    public ResponseEntity<String> async() {
        asyncService.test();
        return ResponseEntity.ok("async");
    }
    @GetMapping("/error")
    public ResponseEntity<String> error() {
         log.error("error");
        return ResponseEntity.ok("error");
    }

}