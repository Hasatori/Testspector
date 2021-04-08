package com.testspector.model.checking.factory;

import com.testspector.model.checking.InspectionInvocationLineResolveStrategy;
import com.testspector.model.checking.java.junit.JUnitInspectionInvocationLineResolveStrategy;
import com.testspector.model.enums.UnitTestFramework;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class InspectionInvocationLineResolveStrategyFactoryTest {

    @Test
    public void getInspectionInvocationLineResolveStrategy_JUnit_ShouldReturnJUnitInspectionInvocationLineResolveStrategy() {
        InspectionInvocationLineResolveStrategyFactory inspectionInvocationLineResolveStrategyFactory = new InspectionInvocationLineResolveStrategyFactory();

        InspectionInvocationLineResolveStrategy inspectionInvocationLineResolveStrategy = inspectionInvocationLineResolveStrategyFactory
                        .getInspectionInvocationLineResolveStrategy(UnitTestFramework.JUNIT)
                        .get();

        Assertions.assertTrue(inspectionInvocationLineResolveStrategy instanceof JUnitInspectionInvocationLineResolveStrategy);
    }

    @Test
    public void getInspectionInvocationLineResolveStrategy_PhpUnitTestingFramework_ShouldBeEmpty() {
        InspectionInvocationLineResolveStrategyFactory inspectionInvocationLineResolveStrategyFactory = new InspectionInvocationLineResolveStrategyFactory();

        Optional<InspectionInvocationLineResolveStrategy> optionalInspectionInvocationLineResolveStrategy = inspectionInvocationLineResolveStrategyFactory
                .getInspectionInvocationLineResolveStrategy(UnitTestFramework.PHP_UNIT);

        Assertions.assertFalse(optionalInspectionInvocationLineResolveStrategy.isPresent());
    }
}
