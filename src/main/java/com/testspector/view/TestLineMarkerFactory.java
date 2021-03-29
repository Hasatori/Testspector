package com.testspector.view;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;
import com.sun.istack.NotNull;
import com.testspector.controller.TestspectorController;
import com.testspector.model.checking.TestLineCrate;

import java.util.Optional;

public class TestLineMarkerFactory implements LineMarkerProvider {

    @Override
    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        TestspectorController testspectorController = element.getProject().getComponent(TestspectorController.class);
        Optional<TestLineCrate> optionalTestLineCrate = testspectorController.resolveTestLineCrate(element);
        return optionalTestLineCrate.map(testLineCrate -> new LineMarkerInfo<>(
                testLineCrate.getLineElement(),
                testLineCrate.getLineElement().getTextRange(),
                CustomIcon.LOGO.getBasic(),
                psiElement -> "Invoke inspection",
                (mouseEvent, psiElement) -> testspectorController.initializeTestspector(psiElement.getParent(),element.toString()),
                GutterIconRenderer.Alignment.RIGHT
        )).orElse(null);
    }
}
