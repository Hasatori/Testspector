package com.testspector.model.checking.java.junit;


import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class NoConditionalLogicJUnitCheckingStrategyTest {


    private Object helperMethodWithIf(Boolean val) {
        Object createObject = null;
        if (val) {
            createObject = "Test1";
        } else {
            createObject = "Test2";
        }

    }

    @Test
    public void testWithForStatement() {
        String exampleString = helperMethodWithIf(true);
        List<String> testingValues = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            testingValues.add("Someval " + i);
        }
        Assert.assertTrue(true);
    }

    @Test
    public void testWithWhileStatement() {
        String exampleString = helperMethodWithSwitch(true);
        List<String> testingValues = new ArrayList<>();
        int i = 50;
        while (i < 50) {
            testingValues.add("Someval " + i);
            i++;
        }
        Assert.assertTrue(true);
    }

    private Object helperMethodWithSwitch(Integer integer) {
        Object createObject = null;
        switch (integer) {
            case 1:
                return "Test1";
            default:
                return "Test2";
        }

    }

    @Test
    public void testWithNoStatement() {
        String exampleString = helperMethodWithSwitch(true);
        String exampleString = helperMethodWithIf(true);


        Assert.assertTrue(true);
    }
}
