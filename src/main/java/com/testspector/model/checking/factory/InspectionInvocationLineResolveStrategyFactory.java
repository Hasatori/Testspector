package com.testspector.model.checking.factory;

import com.testspector.model.checking.InspectionInvocationLineResolveStrategy;
import com.testspector.model.checking.java.junit.JUnitInspectionInvocationLineResolveStrategy;
import com.testspector.model.enums.UnitTestFramework;

import java.util.Optional;

public class InspectionInvocationLineResolveStrategyFactory {


    public Optional<InspectionInvocationLineResolveStrategy> getInspectionInvocationLineResolveStrategy(UnitTestFramework unitTestFramework) {
        if (unitTestFramework == UnitTestFramework.JUNIT) {
            return Optional.of(new JUnitInspectionInvocationLineResolveStrategy());
        }
        return Optional.empty();
    }
}
