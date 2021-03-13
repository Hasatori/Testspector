package com.testspector.model.checking.factory;

import com.testspector.model.checking.java.junit.JUnitUnitTestFrameworkFactory;
import com.testspector.model.enums.ProgrammingLanguage;

import java.util.ArrayList;
import java.util.List;

public class UnitTestFrameworkFactoryProvider {

    public List<UnitTestFrameworkFactory> geUnitTestFrameworkFactory(ProgrammingLanguage programmingLanguage) {
        List<UnitTestFrameworkFactory> factories = new ArrayList<>();
        if (programmingLanguage == ProgrammingLanguage.JAVA) {
            factories.add(new JUnitUnitTestFrameworkFactory());
        }
        return factories;
    }
}
