package com.testspector.gui;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.sun.istack.NotNull;
import com.testspector.Testspector;
import com.testspector.checking.TestResolveStrategy;
import com.testspector.checking.java.junit.JUnitTestResolveStrategy;
import com.testspector.enums.ProgrammingLanguage;
import com.testspector.enums.UnitTestFramework;
import com.testspector.utils.ProgrammingLanguageResolver;
import com.testspector.utils.UnitTestFrameworkResolver;

import java.util.Optional;

public class TestLineMarkerFactory implements LineMarkerProvider {

    @Override
    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        PsiFile psiFile = element.getContainingFile();
        ProgrammingLanguage programmingLanguage = ProgrammingLanguageResolver.resolveProgrammingLanguage(psiFile);
        if (programmingLanguage != null) {
            UnitTestFramework unitTestFramework = UnitTestFrameworkResolver.resolveUnitTestFramework(programmingLanguage, psiFile);
            Optional<TestResolveStrategy> optionalTestResolveStrategy = selectStrategy(programmingLanguage, unitTestFramework);
            if (optionalTestResolveStrategy.isPresent()) {
                PsiElement test = optionalTestResolveStrategy.get().resolveTest(element);
                if (test != null) {
                    return new LineMarkerInfo<>(
                            test,
                            test.getTextRange(),
                            IconLoader.getIcon("/icons/logo.svg"),
                            psiElement -> "Invoke inspection",
                            (mouseEvent, psiElement) -> Testspector.initializeTestspector(psiElement.getParent(), programmingLanguage, unitTestFramework),
                            GutterIconRenderer.Alignment.RIGHT
                    );
                }
            }
        }
        return null;
    }

    private Optional<TestResolveStrategy> selectStrategy(ProgrammingLanguage programmingLanguage, UnitTestFramework unitTestFramework) {
        if (programmingLanguage == ProgrammingLanguage.JAVA && unitTestFramework == UnitTestFramework.JUNIT) {
            return Optional.of(new JUnitTestResolveStrategy());
        }
        return Optional.empty();
    }

}
