package com.testspector.controller;

import com.intellij.codeInspection.LocalInspectionTool;
import com.testspector.model.enums.BestPractice;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

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
                NoGlobalStaticProperties.class
        };
    }

    private static class NoGlobalStaticProperties extends BestPracticeInspection {

        private static final BestPractice bestPractice = BestPractice.NO_GLOBAL_STATIC_PROPERTIES;


        @Override
        public @Nullable
        @Nls
        String getStaticDescription() {
            return String.format("<html>" +
                    "<p>We should not use and modify global static variables across individual tests. " +
                    "The tests share a reference to the same variable, and if one modifies it, it will change the conditions for the next test. " +
                    "If the variable is so-called Immutable, ie it is not possible to change its internal properties, the solution is to make the variable a constant. " +
                    "With such change individual tests can no longer change its reference or its content (an example is the keyword final in Java). " +
                    "If it is possible to change its internal values for a variable, it is recommended to convert it at the level of the test class and set it either in the test itself or using the so-called hook method, which is run before each test. " +
                    "When using the hook method, the variable must be reinitialized.</p>" +
                    "<br/>"+
                    "<a href=\"%s\">Get more information about the rule</a>" +
                    "</html>", bestPractice.getWebPageHyperlink());
        }

        @Override
        public @NonNls
        @NotNull
        String getShortName() {
            return bestPractice.name();
        }

        @NotNull
        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        String[] getGroupPath() {
            return new String[]{"Testspector", "Creating tests", "Independence"};
        }

        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        String getGroupDisplayName() {
            return "Independence";
        }


        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        String getDisplayName() {
            return bestPractice.getDisplayName();
        }

        @Override
        public BestPractice getBestPractice() {
            return bestPractice;
        }
    }

    private static class OnlyOneAssertion extends BestPracticeInspection {


        private static final BestPractice bestPractice = BestPractice.ONLY_ONE_ASSERTION;



        @Override
        public @Nullable
        @Nls
        String getStaticDescription() {
            return String.format("<html>" +
                    "<p>Test should fail for only one reason. If one assertion fails other will not be executed and therefore you will not get overview of all problems.</p>" +
                    "<br/>"+
                    "<a href=\"%s\">Get more information about the rule</a>" +
                    "</html>", BestPractice.AT_LEAST_ONE_ASSERTION.getWebPageHyperlink());
        }

        @Override
        public @NonNls
        @NotNull
        String getShortName() {
            return bestPractice.name();
        }

        @NotNull
        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        String[] getGroupPath() {
            return new String[]{"Testspector", "Creating tests", "Assertions"};
        }

        @Override
        public @NonNls
        @Nullable
        String getGroupKey() {
            return null;
        }

        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        String getGroupDisplayName() {
            return "Assertions";
        }


        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        String getDisplayName() {
            return bestPractice.getDisplayName();
        }

        @Override
        public BestPractice getBestPractice() {
            return bestPractice;
        }
    }

    private static class AtLeastOneAssertion extends BestPracticeInspection {

        private static final BestPractice bestPractice = BestPractice.AT_LEAST_ONE_ASSERTION;


        @Override
        public @Nullable
        @Nls
        String getStaticDescription() {
            return String.format("<html>" +
                    "<p>At least one assertion should be part of the test. In many testing frameworks if there is no assertion method in the test the framework usually reports such test as positive. This will lead to false positive tests.</p>" +
                    "<br/>"+
                    "<a href=\"%s\">Get more information about the rule</a>" +
                    "</html>", BestPractice.AT_LEAST_ONE_ASSERTION.getWebPageHyperlink());
        }

        @Override
        public @NonNls
        @NotNull
        String getShortName() {
            return bestPractice.name();
        }

        @NotNull
        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        String[] getGroupPath() {
            return new String[]{"Testspector", "Creating tests", "Assertions"};
        }

        @Override
        public @NonNls
        @Nullable
        String getGroupKey() {
            return null;
        }

        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        String getGroupDisplayName() {
            return "Assertions";
        }


        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        String getDisplayName() {
            return bestPractice.getDisplayName();
        }

        @Override
        public BestPractice getBestPractice() {
            return bestPractice;
        }
    }

    private static class NoConditionalLogicInspection extends BestPracticeInspection {

        private static final BestPractice bestPractice = BestPractice.NO_CONDITIONAL_LOGIC;


        @Override
        public @Nullable
        @Nls
        String getStaticDescription() {
            return String.format("<html>" +
                    "<p>Conditional logic in the form of if, else, for, or while should not be part of the test code. " +
                    "In general, it increases the complexity of the test method, which complicates its comprehensibility, readability. " +
                    "It is very hard to determine what is exactly tested as the person reading the test has to think about which conditional branch is going to be executed. " +
                    "It leads to the skipping of some verification methods and thus gives the illusion of correctness. " +
                    "A general solution to this problem is to extract all conditional branches into separate tests. Another option is to use the so-called parameterized tests, each option is represented by one set of parameters. " +
                    "</p>" +
                    "<br/>"+
                    "<a href=\"%s\">Get more information about the rule</a>" +
                    "</html>", BestPractice.AT_LEAST_ONE_ASSERTION.getWebPageHyperlink());
        }

        @Override
        public @NonNls
        @NotNull
        String getShortName() {
            return bestPractice.name();
        }

        @NotNull
        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        String[] getGroupPath() {
            return new String[]{"Testspector", "Creating tests", "Conditional logic"};
        }

        @Override
        public @NonNls
        @Nullable
        String getGroupKey() {
            return null;
        }

        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        String getGroupDisplayName() {
            return "Conditional logic";
        }


        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        String getDisplayName() {
            return bestPractice.getDisplayName();
        }

        @Override
        public BestPractice getBestPractice() {
            return bestPractice;
        }
    }

    private static class SetupTestNamingStrategyInspection extends BestPracticeInspection {

        private static final BestPractice bestPractice = BestPractice.SETUP_A_TEST_NAMING_STRATEGY;


        @Override
        public @Nullable
        @Nls
        String getStaticDescription() {
            return String.format("<html>" +
                    "<p>The test name is more or less the same as the tested method. " +
                    "This says nothing about tests scenario. You should setup a clear strategy" +
                    " for naming your tests so that the person reading then knows what is tested</p>" +
                    "<p>There are several recommended strategies which can be used to name your tests:</p>" +
                    "<ul>" +
                    "<li>doingSomeOperationGeneratesSomeResult</li>" +
                    "<li>someResultOccursUnderSomeCondition</li>" +
                    "<li>given-when-then</li>" +
                    "<li>givenSomeContextWhenDoingSomeBehaviorThenSomeResultOccurs</li>" +
                    "<li>whatIsTested_conditions_expectedResult</li>" +
                    "</ul>" +
                    "<p>Chosen naming strategy is subjective. The key thing to remember is that name of the test should say:</p>" +
                    "<ul>" +
                    "<li>What is tests</li>" +
                    "<li>What are the conditions</li>" +
                    "<li>What is expected result</li>" +
                    "</ul>" +
                    "<br/>"+
                    "<a href=\"%s\">Get more information about the rule</a>" +
                    "</html>", bestPractice.getWebPageHyperlink());
        }

        @Override
        public @NonNls
        @NotNull
        String getShortName() {
            return bestPractice.name();
        }

        @NotNull
        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        String[] getGroupPath() {
            return new String[]{"Testspector", "Creating tests", "Naming conventions"};
        }

        @Override
        public @NonNls
        @Nullable
        String getGroupKey() {
            return null;
        }

        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        String getGroupDisplayName() {
            return "Naming conventions";
        }


        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        String getDisplayName() {
            return bestPractice.getDisplayName();
        }

        @Override
        public BestPractice getBestPractice() {
            return bestPractice;
        }
    }

    private static class TestOnlyPublicBehaviourInspection extends BestPracticeInspection {

        private static final BestPractice bestPractice = BestPractice.TEST_ONLY_PUBLIC_BEHAVIOUR;


        @Override
        public @Nullable
        @Nls
        String getStaticDescription() {
            return String.format("<html>" +
                    "<p>You should always test only the public behavior of the tested system. That is usually expressed using public methods. " +
                    "The implementation of private methods or methods private to the package is very often changed, methods are deleted or added, regardless of the behavior of the system as a whole. " +
                    "Private methods are only an auxiliary tool to ensure the public behavior of the tested system. " +
                    "Their testing creates a large number of dependencies between the code and the tests, and in the long run this leads to difficult maintenance of the tests and the need for their " +
                    "frequent modification and updating. If private methods contain complex behavior it might seem that writing a separate test for such behaviour is a good idea. In fact that is just sign of that you are breaking single responsibility rule " +
                    "and private method should be managed by some new object or function. Thus, such behavior should be extracted into a separate class and the tested system should only communicate with it. </p>" +
                    "<br/>"+
                    "<a href=\"%s\">Get more information about the rule</a>" +
                    "</html>", bestPractice.getWebPageHyperlink());
        }

        @Override
        public @NonNls
        @NotNull
        String getShortName() {
            return bestPractice.name();
        }

        @NotNull
        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        String[] getGroupPath() {
            return new String[]{"Testspector", "Defining tests", "Code coverage"};
        }

        @Override
        public @NonNls
        @Nullable
        String getGroupKey() {
            return null;
        }

        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        String getGroupDisplayName() {
            return "Code coverage";
        }


        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        String getDisplayName() {
            return bestPractice.getDisplayName();
        }

        @Override
        public BestPractice getBestPractice() {
            return bestPractice;
        }
    }

    private static class CatchExceptionsUsingFrameworkTools extends BestPracticeInspection {

        private static final BestPractice bestPractice = BestPractice.CATCH_TESTED_EXCEPTIONS_USING_FRAMEWORK_TOOLS;


        @Override
        public @Nullable
        @Nls
        String getStaticDescription() {
            return String.format("<html>" +
                    "<p>Tests should not contain try catch block. " +
                    "These blocks are redundant it is inflating the test method and make test harder to read and understand. </p>" +
                    "<br/>"+
                    "<a href=\"%s\">Get more information about the rule</a>" +
                    "</html>", bestPractice.getWebPageHyperlink());
        }

        @Override
        public @NonNls
        @NotNull
        String getShortName() {
            return bestPractice.name();
        }

        @NotNull
        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        String[] getGroupPath() {
            return new String[]{"Testspector", "Creating tests", "Testing exceptions"};
        }

        @Override
        public @NonNls
        @Nullable
        String getGroupKey() {
            return null;
        }

        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        String getGroupDisplayName() {
            return "Testing exceptions";
        }


        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        String getDisplayName() {
            return bestPractice.getDisplayName();
        }

        @Override
        public BestPractice getBestPractice() {
            return bestPractice;
        }
    }

}
