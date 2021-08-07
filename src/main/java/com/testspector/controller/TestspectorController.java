package com.testspector.controller;

import com.intellij.analysis.AnalysisScope;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ex.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.factory.BestPracticeCheckingStrategyFactory;
import com.testspector.model.checking.factory.BestPracticeCheckingStrategyFactoryProvider;
import com.testspector.model.checking.factory.ProgrammingLanguageFactory;
import com.testspector.model.checking.factory.UnitTestFrameworkFactoryProvider;
import com.testspector.model.enums.BestPractice;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;
import com.testspector.view.inspection.BestPracticeInspection;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class TestspectorController {

    private final Project project;
    private static Pair<LocalInspectionToolSession, BestPracticeCheckingStrategyFactory> sessionBestPracticeCheckingStrategyFactoryPair = Pair.of(null, null);

    public TestspectorController(Project project) {
        this.project = project;
    }

    public void initializeTestspector(VirtualFile virtualFile) {
        InspectionProfileImpl inspectionProfile = InspectionProjectProfileManager.getInstance(this.project).getCurrentProfile();
        List<ScopeToolState> scopeToolStates = inspectionProfile
                .getAllTools()
                .stream()
                .filter(scopeToolState -> (scopeToolState.getTool().getTool() instanceof BestPracticeInspection) && scopeToolState.isEnabled())
                .collect(Collectors.toList());
        FileDocumentManager.getInstance().saveAllDocuments();
        final GlobalInspectionContextImpl inspectionContext = ((InspectionManagerEx) InspectionManager.getInstance(project)).createNewGlobalContext();
        InspectionToolsSupplier inspectionToolsSupplier = new InspectionToolsSupplier.Simple(
                scopeToolStates
                        .stream()
                        .map(ScopeToolState::getTool)
                        .collect(Collectors.toList())
        );
        inspectionProfile = new InspectionProfileImpl("Testspector", inspectionToolsSupplier, inspectionProfile);
        inspectionContext.setExternalProfile(inspectionProfile);
        AnalysisScope scope = new AnalysisScope(project, Collections.singletonList(virtualFile));
        inspectionContext.setCurrentScope(scope);
        scope.setSearchInLibraries(false);
        scope.setIncludeTestSource(true);
        inspectionContext.doInspections(scope);
    }


    public List<BestPracticeViolation> inspectFile(PsiFile file, BestPractice bestPractice, LocalInspectionToolSession session) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        Optional<ProgrammingLanguage> optionalProgrammingLanguage = project.getComponent(ProgrammingLanguageFactory.class)
                .getProgrammingLanguage(file);
        if (optionalProgrammingLanguage.isPresent()) {
            ProgrammingLanguage programmingLanguage = optionalProgrammingLanguage.get();
            List<UnitTestFramework> unitTestFrameworks = gatherUnitTestFrameworks(file, programmingLanguage);
            for (UnitTestFramework unitTestFramework : unitTestFrameworks) {
                Optional<BestPracticeCheckingStrategy<PsiElement>> optionalBestPracticeCheckingStrategy =
                        tryToGetCheckingStrategy(session, bestPractice, unitTestFramework, programmingLanguage);
                List<BestPracticeViolation> foundViolations = optionalBestPracticeCheckingStrategy
                        .map(checkingStrategy -> checkingStrategy.checkBestPractices(file))
                        .orElse(new ArrayList<>());
                bestPracticeViolations.addAll(foundViolations);
            }
        }
        return bestPracticeViolations;
    }

    private List<UnitTestFramework> gatherUnitTestFrameworks(PsiFile file, ProgrammingLanguage programmingLanguage) {
        return project
                .getComponent(UnitTestFrameworkFactoryProvider.class)
                .geUnitTestFrameworkFactory(programmingLanguage)
                .stream()
                .map(unitTestFrameworkFactory -> unitTestFrameworkFactory.getUnitTestFramework(file))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<BestPracticeCheckingStrategy<PsiElement>> tryToGetCheckingStrategy(LocalInspectionToolSession session, BestPractice bestPractice, UnitTestFramework unitTestFramework, ProgrammingLanguage programmingLanguage) {
        Optional<BestPracticeCheckingStrategy<PsiElement>> optionalBestPracticeCheckingStrategy;
        if (session == sessionBestPracticeCheckingStrategyFactoryPair.getLeft()) {
            optionalBestPracticeCheckingStrategy = Optional.ofNullable(sessionBestPracticeCheckingStrategyFactoryPair.getRight().getBestPracticeCheckingStrategy(bestPractice));
        } else {
            optionalBestPracticeCheckingStrategy = project
                    .getComponent(BestPracticeCheckingStrategyFactoryProvider.class)
                    .getBestPracticeCheckingStrategyFactory(programmingLanguage, unitTestFramework)
                    .map(bestPracticeCheckingStrategyFactory -> {
                        sessionBestPracticeCheckingStrategyFactoryPair = Pair.of(session, bestPracticeCheckingStrategyFactory);
                        return bestPracticeCheckingStrategyFactory.getBestPracticeCheckingStrategy(bestPractice);
                    });
        }
        return optionalBestPracticeCheckingStrategy;
    }
}
