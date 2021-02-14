package com.testspector.checking.java.junit;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.testspector.checking.BestPracticeViolation;
import com.testspector.checking.java.JavaBestPracticeCheckingStrategy;
import com.testspector.enums.ProgrammingLanguage;
import com.testspector.enums.UnitTestFramework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.testspector.enums.BestPractice.*;

public class JUnitBestPracticeCheckingStrategy extends JavaBestPracticeCheckingStrategy {

    public JUnitBestPracticeCheckingStrategy() {
        super(ProgrammingLanguage.JAVA,
                Arrays.asList(
                        TEST_ONLY_PUBLIC_BEHAVIOUR,
                        NO_SIMPLE_TESTS,
                        AT_LEAST_ONE_ASSERTION,
                        ONLY_ONE_ASSERTION,
                        NO_GLOBAL_STATIC_PROPERTIES,
                        CREATE_CUSTOM_DATA_AND_SOURCES,
                        SETUP_A_TEST_NAMING_STRATEGY,
                        CATCH_EXCEPTIONS_USING_FRAMEWORK_TOOLS,
                        NO_CONDITIONAL_LOGIC,
                        THREE_PHASE_TEST_STRUCTURE
                )
                , UnitTestFramework.JUNIT);
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiFile psiFile) {
        return new ArrayList<>();
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiFile> psiFiles) {
        return new ArrayList<>();
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiElement element) {
        return new ArrayList<>();
    }
}
