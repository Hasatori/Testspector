package com.testspector.model.checking.factory;

import com.testspector.model.checking.java.junit.JUnitUnitTestFrameworkFactory;
import com.testspector.model.enums.ProgrammingLanguage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class UnitTestFrameworkFactoryProviderTest {


    @Test
    public void getUnitTestFrameworkFactory_Java_ShouldReturnListWithJustJUnitFrameworkFactory() {
        UnitTestFrameworkFactoryProvider unitTestFrameworkFactoryProvider = new UnitTestFrameworkFactoryProvider();

        List<UnitTestFrameworkFactory> unitTestFrameworkFactories = unitTestFrameworkFactoryProvider
                .geUnitTestFrameworkFactory(ProgrammingLanguage.JAVA);

        Assertions.assertTrue(unitTestFrameworkFactories.get(0) instanceof JUnitUnitTestFrameworkFactory);
    }

    @Test
    public void getUnitTestFrameworkFactory_Typescript_ShouldReturnEmptyList() {
        UnitTestFrameworkFactoryProvider unitTestFrameworkFactoryProvider = new UnitTestFrameworkFactoryProvider();

        List<UnitTestFrameworkFactory> unitTestFrameworkFactories = unitTestFrameworkFactoryProvider
                .geUnitTestFrameworkFactory(ProgrammingLanguage.TYPESCRIPT);

        Assertions.assertTrue(unitTestFrameworkFactories.isEmpty());
    }
}
