package com.testspector.model.checking;

import com.intellij.psi.PsiElement;
import com.testspector.model.checking.java.junit.JUnitUnitTestFrameworkResolveStrategy;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;

import java.util.Optional;

public class UnitTestFrameworkFactory {


    public Optional<UnitTestFramework> getUnitTestFramework(ProgrammingLanguage programmingLanguage, PsiElement psiElement) {
        if (programmingLanguage != null) {
            switch (programmingLanguage) {
                case JAVA:
                    if (new JUnitUnitTestFrameworkResolveStrategy().canResolveFromPsiElement(psiElement)) {
                        return Optional.of(UnitTestFramework.JUNIT);
                    }

            }
        }
        return Optional.empty();
    }

}
