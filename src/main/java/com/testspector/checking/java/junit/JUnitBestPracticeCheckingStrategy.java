package com.testspector.checking.java.junit;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.testspector.checking.BestPracticeViolation;
import com.testspector.checking.java.JavaBestPracticeCheckingStrategy;
import com.testspector.enums.BestPractice;
import com.testspector.enums.ProgrammingLanguage;
import com.testspector.enums.UnitTestFramework;

import java.util.List;

public class JUnitBestPracticeCheckingStrategy extends JavaBestPracticeCheckingStrategy {

    public JUnitBestPracticeCheckingStrategy(ProgrammingLanguage supportedProgrammingLanguage, List<BestPractice> supportedRules, UnitTestFramework supportedFramework) {
        super(supportedProgrammingLanguage, supportedRules, supportedFramework);
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiFile psiFile) {
        return null;
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(List<PsiFile> psiFiles) {
        return null;
    }

    @Override
    public List<BestPracticeViolation> checkBestPractices(PsiElement element) {
        return null;
    }
}
