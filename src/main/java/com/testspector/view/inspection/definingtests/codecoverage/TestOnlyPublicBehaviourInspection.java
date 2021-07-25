package com.testspector.view.inspection.definingtests.codecoverage;

import com.testspector.model.enums.BestPractice;
import com.testspector.view.inspection.BestPracticeInspection;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestOnlyPublicBehaviourInspection extends CodeCoverageInspection {

    @Override
    public @Nullable
    @Nls
    String getStaticDescription() {
        return String.format(
                "<p>You should always test only the public behavior of the tested system. That is usually expressed using public methods. " +
                "The implementation of private methods or methods private to the package is very often changed, methods are deleted or added, regardless of the behavior of the system as a whole. " +
                "Private methods are only an auxiliary tool to ensure the public behavior of the tested system. " +
                "Their testing creates a large number of dependencies between the code and the tests, and in the long run this leads to difficult maintenance of the tests and the need for their " +
                "frequent modification and updating. If private methods contain complex behavior it might seem that writing a separate test for such behaviour is a good idea. In fact that is just sign of that you are breaking single responsibility rule " +
                "and private method should be managed by some new object or function. Thus, such behavior should be extracted into a separate class and the tested system should only communicate with it. </p>" +
                "<br/>" +
                "<a href=\"%s\">Get more information about the rule</a>"
                , getBestPractice().getWebPageHyperlink());
    }

    @NotNull
    @Override
    public BestPractice getBestPractice() {
        return BestPractice.TEST_ONLY_PUBLIC_BEHAVIOUR;
    }
}
