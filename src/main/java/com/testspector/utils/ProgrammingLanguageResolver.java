package com.testspector.utils;

import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.PsiFile;
import com.testspector.enums.ProgrammingLanguage;


public class ProgrammingLanguageResolver {

    private ProgrammingLanguageResolver() {
    }

    public static ProgrammingLanguage resolveProgrammingLanguage(PsiFile psiFile) {
        Language language = psiFile.getLanguage();
        if (language instanceof JavaLanguage) {
            return ProgrammingLanguage.JAVA;
        }
        return null;
    }

}
