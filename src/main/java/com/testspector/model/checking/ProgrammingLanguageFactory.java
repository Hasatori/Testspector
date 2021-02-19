package com.testspector.model.checking;

import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.lang.javascript.dialects.TypeScriptLanguageDialect;
import com.intellij.psi.PsiElement;
import com.jetbrains.php.lang.PhpLanguage;
import com.testspector.model.enums.ProgrammingLanguage;

import java.util.Optional;


public class ProgrammingLanguageFactory {


    public Optional<ProgrammingLanguage> resolveProgrammingLanguage(PsiElement psiElement) {
        if (psiElement != null) {
            Language language = psiElement.getContainingFile().getLanguage();
            if (language instanceof JavaLanguage) {
                return Optional.of(ProgrammingLanguage.JAVA);
            } else if (language instanceof TypeScriptLanguageDialect) {
                return Optional.of(ProgrammingLanguage.TYPESCRIPT);
            } else if (language instanceof PhpLanguage) {
                return Optional.of(ProgrammingLanguage.PHP);
            }
        }
        return Optional.empty();
    }

}
