package com.testspector.controller;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestSpectorExecutorServiceFactory {

    ExecutorService getTestSpectorExecutorService(int maxThreadCount) {
        return Executors.newFixedThreadPool(maxThreadCount);
    }
}
