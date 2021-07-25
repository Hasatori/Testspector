package com.testspector.view.inspection.creatingtests.independence;

import com.testspector.model.enums.BestPractice;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NoGlobalStaticProperties extends IndependenceInspection {

    @Override
    public @Nullable
    @Nls
    String getStaticDescription() {
        return String.format(
                "<p>We should not use and modify global static variables across individual tests. " +
                "The tests share a reference to the same variable, and if one modifies it, it will change the conditions for the next test. " +
                "If the variable is so-called Immutable, ie it is not possible to change its internal properties, the solution is to make the variable a constant. " +
                "With such change individual tests can no longer change its reference or its content (an example is the keyword final in Java). " +
                "If it is possible to change its internal values for a variable, it is recommended to convert it at the level of the test class and set it either in the test itself or using the so-called hook method, which is run before each test. " +
                "When using the hook method, the variable must be reinitialized.</p>" +
                "<br/>" +
                "<a href=\"%s\">Get more information about the rule</a>"
                 , getBestPractice().getWebPageHyperlink());
    }

    @NotNull
    @Override
    public BestPractice getBestPractice() {
        return BestPractice.NO_GLOBAL_STATIC_PROPERTIES;
    }
}
