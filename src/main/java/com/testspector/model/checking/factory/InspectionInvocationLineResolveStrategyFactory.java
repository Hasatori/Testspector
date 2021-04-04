package com.testspector.model.checking.factory;

import com.testspector.model.checking.InspectionInvocationLineResolveStrategy;
import com.testspector.model.checking.groovy.spock.SpockInspectionInvocationLineResolveStrategy;
import com.testspector.model.checking.java.junit.JUnitInspectionInvocationLineResolveStrategy;
import com.testspector.model.enums.UnitTestFramework;

import java.util.Optional;

public class InspectionInvocationLineResolveStrategyFactory {


    public Optional<InspectionInvocationLineResolveStrategy> getInspectionInvocationLineResolveStrategy(UnitTestFramework unitTestFramework) {
        if (unitTestFramework == UnitTestFramework.JUNIT) {
            return Optional.of(new JUnitInspectionInvocationLineResolveStrategy());
        } else if (unitTestFramework == UnitTestFramework.SPOCK) {
            return Optional.of(new SpockInspectionInvocationLineResolveStrategy());
        }
        return Optional.empty();
    }
}
