package com.testspector.controller;

import com.intellij.codeInspection.LocalInspectionTool;
import com.testspector.view.inspection.creatingtests.assertions.AtLeastOneAssertion;
import com.testspector.view.inspection.creatingtests.assertions.OnlyOneAssertion;
import com.testspector.view.inspection.creatingtests.conditionallogic.NoConditionalLogicInspection;
import com.testspector.view.inspection.creatingtests.independence.NoGlobalStaticProperties;
import com.testspector.view.inspection.creatingtests.namingconventions.SetupTestNamingStrategyInspection;
import com.testspector.view.inspection.creatingtests.testingexceptions.CatchExceptionsUsingFrameworkTools;
import com.testspector.view.inspection.creatingtests.testingexceptions.SetGeneralLevelForTestException;
import com.testspector.view.inspection.definingtests.codecoverage.TestOnlyPublicBehaviourInspection;
import org.jetbrains.annotations.NotNull;

public class InspectionToolProvider implements com.intellij.codeInspection.InspectionToolProvider {

    @NotNull
    @Override
    public Class<? extends LocalInspectionTool>[] getInspectionClasses() {
        return new Class[]{
                AtLeastOneAssertion.class,
                OnlyOneAssertion.class,
                NoConditionalLogicInspection.class,
                SetupTestNamingStrategyInspection.class,
                TestOnlyPublicBehaviourInspection.class,
                CatchExceptionsUsingFrameworkTools.class,
                SetGeneralLevelForTestException.class,
                NoGlobalStaticProperties.class
        };
    }

}
