package com.testspector.view.inspection.creatingtests.testingexceptions;

import com.testspector.model.enums.BestPractice;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SetGeneralLevelForTestException extends TestingExceptionsInspection{

    @Override
    public @Nullable
    @Nls
    String getStaticDescription() {
        return String.format(
                "<p>In cases where throwing an exception is not part of the test but is a possible product of one of the executed methods," +
                        " we should again not use try catch blocks, but the exception should be caught by the test method itself. " +
                        "The reason is again improving the readability of the test code by reducing its " +
                        "length and also ensuring correct behaviour in case of exception thrown. " +
                        "The exception will thus be caught by the test framework itself and the test will fail. " +
                        "In this respect, it is also very important to pay attention to the level of exception that " +
                        "the test method will catch. It should always be the level of the exception at the top of the " +
                        "hierarchy, for example for the c++ programming language it is the exception class. " +
                        "This approach ensures easier maintainability of the tests, because if the production code is " +
                        "modified and a certain method starts throwing a different type of exception, the tests will " +
                        "not need to be changed as the top level will catch this case</p>" +
                        "<br/>" +
                        "<a href=\"%s\">Get more information about the rule</a>"
                , getBestPractice().getWebPageHyperlink());
    }


    @Override
    public @NotNull BestPractice getBestPractice() {
        return BestPractice.SET_EXCEPTION_CLASS_ON_THE_TOP_OF_THE_HIERARCHY;
    }
}
