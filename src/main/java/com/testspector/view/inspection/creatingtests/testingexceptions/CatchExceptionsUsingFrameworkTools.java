package com.testspector.view.inspection.creatingtests.testingexceptions;

import com.testspector.model.enums.BestPractice;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CatchExceptionsUsingFrameworkTools extends TestingExceptionsInspection {

    @Override
    public @Nullable
    @Nls
    String getStaticDescription() {
        return String.format(
                "<p>It is not recommended to test exceptions by using try and catch block. " +
                        "Using the blocks only is redundant and it make test method bigger and makes it harder to read and understand test. " +
                        "This approach can lead to so-called Never failing tests which happens in case when we forget to fail test in case when exception has not been thrown.</p>" +
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
