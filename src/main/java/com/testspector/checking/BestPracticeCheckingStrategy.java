package com.testspector.checking;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.testspector.enums.BestPractice;
import com.testspector.enums.ProgrammingLanguage;
import com.testspector.enums.UnitTestFramework;

import java.util.List;

public abstract class BestPracticeCheckingStrategy {

    private final ProgrammingLanguage supportedProgrammingLanguage;

    private final List<BestPractice> supportedRules;

    private final UnitTestFramework supportedFramework;

    public BestPracticeCheckingStrategy(ProgrammingLanguage supportedProgrammingLanguage, List<BestPractice> supportedRules, UnitTestFramework supportedFramework) {
        this.supportedProgrammingLanguage = supportedProgrammingLanguage;
        this.supportedRules = supportedRules;
        this.supportedFramework = supportedFramework;
    }

    public abstract List<BestPracticeViolation> checkBestPractices(PsiFile psiFile);

    public abstract List<BestPracticeViolation> checkBestPractices(List<PsiFile> psiFiles);

    public List<BestPracticeViolation> checkBestPractices(PsiElement element) {
        return null;
    }

}
