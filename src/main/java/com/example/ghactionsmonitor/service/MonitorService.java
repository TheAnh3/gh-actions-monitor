package com.example.ghactionsmonitor.service;

import org.springframework.stereotype.Service;
@Service
public class MonitorService {
    public void startMonitoring(String repo, String token){
        System.out.println("Starting monitoring for repo: " + repo);
    }
}
