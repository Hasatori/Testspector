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
                NoCoditionalLogicInspection.class,
                SetupTestNamingStrategyInspection.class,
                TestOnlyPublicBehaviourInspection.class,
                CatchExceptionsUsingFrameworkTools.class,
                NoDeadCode.class
        };
    }

    private static class NoDeadCode extends BestPracticeInspection {

        private static final BestPractice bestPractice = BestPractice.NO_DEAD_CODE;
        static {
            fileListHashMap.put(bestPractice,new HashMap<>());
        }


        @Override
        public @Nullable
        @Nls
        String getStaticDescription() {
            return String.format("<html>" +
                    "<p>No dead code</p>" +
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
            return new String[]{"Testspector", "Creating tests", "Other"};
        }

        @Override
        public @NonNls
        @Nullable
        String getGroupKey() {
            return "test";
        }

        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        String getGroupDisplayName() {
            return "";
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
        static {
            fileListHashMap.put(bestPractice,new HashMap<>());
        }


        @Override
        public @Nullable
        @Nls
        String getStaticDescription() {
            return String.format("<html>" +
                    "<p>Only one assertion should be part of the test</p>" +
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
            return "test";
        }

        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        String getGroupDisplayName() {
            return "";
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
        static {
            fileListHashMap.put(bestPractice,new HashMap<>());
        }

        @Override
        public @Nullable
        @Nls
        String getStaticDescription() {
            return String.format("<html>" +
                    "<p>At least one assertion should be part of the test</p>" +
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
            return "test";
        }

        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        String getGroupDisplayName() {
            return "";
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


    private static class NoCoditionalLogicInspection extends BestPracticeInspection {

        private static final BestPractice bestPractice = BestPractice.NO_CONDITIONAL_LOGIC;
        static {
            fileListHashMap.put(bestPractice,new HashMap<>());
        }

        @Override
        public @Nullable
        @Nls
        String getStaticDescription() {
            return "No conditional logic should be part of the tests";
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
            return "test";
        }

        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        String getGroupDisplayName() {
            return "";
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
        static {
            fileListHashMap.put(bestPractice,new HashMap<>());
        }


        @Override
        public @Nullable
        @Nls
        String getStaticDescription() {
            return String.format("<html>" +
                    "<p>Setup a test naming strategy</p>" +
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
            return "test";
        }

        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        String getGroupDisplayName() {
            return "";
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
        static {
            fileListHashMap.put(bestPractice,new HashMap<>());
        }


        @Override
        public @Nullable
        @Nls
        String getStaticDescription() {
            return String.format("<html>" +
                    "<p>Test only public behaviour</p>" +
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
            return "test";
        }

        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        String getGroupDisplayName() {
            return "";
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
        static {
            fileListHashMap.put(bestPractice,new HashMap<>());
        }


        @Override
        public @Nullable
        @Nls
        String getStaticDescription() {
            return String.format("<html>" +
                    "<p>Test only public behaviour</p>" +
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
            return "test";
        }

        @Override
        public @Nls(capitalization = Nls.Capitalization.Sentence)
        @NotNull
        String getGroupDisplayName() {
            return "";
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
