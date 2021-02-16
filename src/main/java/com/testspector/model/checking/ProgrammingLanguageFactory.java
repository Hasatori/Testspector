package com.testspector.model.checking;

import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.PsiElement;
import com.testspector.model.enums.ProgrammingLanguage;

import java.util.Optional;


public class ProgrammingLanguageFactory {


    public Optional<ProgrammingLanguage> resolveProgrammingLanguage(PsiElement psiElement) {
        Language language = psiElement.getContainingFile().getLanguage();
        if (language instanceof JavaLanguage) {
            return Optional.of(ProgrammingLanguage.JAVA);
        }
        return Optional.empty();
    }

}
