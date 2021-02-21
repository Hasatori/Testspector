package com.testspector.view;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.sun.istack.NotNull;
import com.testspector.controller.TestspectorController;
import com.testspector.model.checking.ProgrammingLanguageFactory;
import com.testspector.model.checking.UnitTestLineResolveStrategy;
import com.testspector.model.checking.java.junit.JUnitUnitTestLineLineResolveStrategy;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;

import java.util.List;
import java.util.Optional;

import static com.testspector.Configuration.UNIT_TEST_FRAMEWORK_RESOLVE_STRATEGY_FACTORY;

public class TestLineMarkerFactory implements LineMarkerProvider {


    @Override
    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        Optional<ProgrammingLanguage> optionalProgrammingLanguage = new ProgrammingLanguageFactory().resolveProgrammingLanguage(element);
        if (optionalProgrammingLanguage.isPresent()) {
            List<UnitTestFramework> unitTestFrameworks = UNIT_TEST_FRAMEWORK_RESOLVE_STRATEGY_FACTORY.getUnitTestFrameworks(optionalProgrammingLanguage.get(), element);
            if (!unitTestFrameworks.isEmpty()) {
                UnitTestFramework unitTestFramework = unitTestFrameworks.get(0);
                Optional<UnitTestLineResolveStrategy> optionalTestResolveStrategy = selectStrategy(optionalProgrammingLanguage.get(), unitTestFramework);
                if (optionalTestResolveStrategy.isPresent()) {
                    Optional<PsiElement> optionalTest = optionalTestResolveStrategy.get().resolveTestLine(element);
                    if (optionalTest.isPresent()) {
                        return new LineMarkerInfo<>(
                                optionalTest.get(),
                                optionalTest.get().getTextRange(),
                                Icons.LOGO,
                                psiElement -> "Invoke inspection",
                                (mouseEvent, psiElement) -> TestspectorController.initializeTestspector(element.getProject(), psiElement.getParent(), optionalProgrammingLanguage.get(), unitTestFramework),
                                GutterIconRenderer.Alignment.RIGHT
                        );
                    }
                }
            }
        }
        return null;
    }

    private Optional<UnitTestLineResolveStrategy> selectStrategy(ProgrammingLanguage programmingLanguage, UnitTestFramework unitTestFramework) {
        if (programmingLanguage == ProgrammingLanguage.JAVA && unitTestFramework == UnitTestFramework.JUNIT) {
            return Optional.of(new JUnitUnitTestLineLineResolveStrategy());
        }
        return Optional.empty();
    }

}
