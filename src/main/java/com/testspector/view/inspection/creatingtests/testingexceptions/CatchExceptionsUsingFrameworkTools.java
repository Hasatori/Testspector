package com.testspector.view.inspection.creatingtests.testingexceptions;

import com.testspector.model.enums.BestPractice;
import com.testspector.view.inspection.BestPracticeInspection;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CatchExceptionsUsingFrameworkTools extends TestingExceptionsInspection {

    @Override
    public @Nullable
    @Nls
    String getStaticDescription() {
        return String.format(
                "<p>Tests should not contain try catch block. " +
                "These blocks are redundant it is inflating the test method and make test harder to read and understand. </p>" +
                "<br/>" +
                "<a href=\"%s\">Get more information about the rule</a>"
                , getBestPractice().getWebPageHyperlink());
    }

    @NotNull
    @Override
    public BestPractice getBestPractice() {
        return BestPractice.CATCH_TESTED_EXCEPTIONS_USING_FRAMEWORK_TOOLS;
    }
}
