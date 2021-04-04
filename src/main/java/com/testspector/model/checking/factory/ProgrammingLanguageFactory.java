package com.testspector.model.checking.factory;

import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.javascript.dialects.TypeScriptLanguageDialect;
import com.intellij.psi.PsiElement;
import com.testspector.model.enums.ProgrammingLanguage;
import org.jetbrains.plugins.groovy.GroovyLanguage;

import java.util.Optional;


public class ProgrammingLanguageFactory {


    public Optional<ProgrammingLanguage> getProgrammingLanguage(PsiElement psiElement) {
        if (psiElement != null) {
            Language language = psiElement.getContainingFile().getLanguage();
            if (language instanceof JavaLanguage) {
                return Optional.of(ProgrammingLanguage.JAVA);
            } else if (language instanceof TypeScriptLanguageDialect) {
                return Optional.of(ProgrammingLanguage.TYPESCRIPT);
            } else if (language instanceof GroovyLanguage) {
                return Optional.of(ProgrammingLanguage.GROOVY);
            }
        }
        return Optional.empty();
    }

}
