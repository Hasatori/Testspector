package com.testspector.controller;


import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.factory.BestPracticeCheckingStrategyFactoryProvider;
import com.testspector.model.checking.factory.ProgrammingLanguageFactory;
import com.testspector.model.checking.factory.UnitTestFrameworkFactoryProvider;
import com.testspector.model.enums.BestPractice;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public abstract class BestPracticeInspection extends LocalInspectionTool {

    public abstract BestPractice getBestPractice();


    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @Override
    public @NotNull
    PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {

        return new PsiElementVisitor() {

            @Override
            public void visitFile(@NotNull PsiFile file) {
                Project project = file.getProject();
                List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
                Optional<ProgrammingLanguage> optionalProgrammingLanguage = project.getService(ProgrammingLanguageFactory.class)
                        .getProgrammingLanguage(file);
                if (optionalProgrammingLanguage.isPresent()) {
                    List<UnitTestFramework> unitTestFrameworks = project
                            .getService(UnitTestFrameworkFactoryProvider.class)
                            .geUnitTestFrameworkFactory(optionalProgrammingLanguage.get())
                            .stream()
                            .map(unitTestFrameworkFactory -> unitTestFrameworkFactory.getUnitTestFramework(file))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
                    if (unitTestFrameworks.size() > 0) {
                        for (UnitTestFramework unitTestFramework : unitTestFrameworks) {
                            Optional<BestPracticeCheckingStrategy<PsiElement>> optionalBestPracticeCheckingStrategy = project
                                    .getService(BestPracticeCheckingStrategyFactoryProvider.class)
                                    .getBestPracticeCheckingStrategyFactory(optionalProgrammingLanguage.get(), unitTestFramework)
                                    .map(bestPracticeCheckingStrategyFactory -> bestPracticeCheckingStrategyFactory.getBestPracticeCheckingStrategy(getBestPractice()));
                            if (optionalBestPracticeCheckingStrategy.isPresent()) {
                                List<BestPracticeViolation> foundViolations = optionalBestPracticeCheckingStrategy
                                        .get()
                                        .checkBestPractices(file);
                                bestPracticeViolations.addAll(foundViolations);
                            }
                        }
                    }
                }
                HashMap<PsiElement, List<ProblemDescriptor>> problemDescriptorsHashMap = new HashMap<>();
                bestPracticeViolations.forEach(bestPracticeViolation -> {
                    String problemDescriptionTemplate = String.format("<html>" +
                                    "<p>%s</p>" +
                                    "%s" +
                                    "<a href=\"%s\">Get more information about the rule</a></html>",
                            bestPracticeViolation.getProblemDescription(),
                            Optional.ofNullable(bestPracticeViolation.getHints()).isPresent() && !bestPracticeViolation.getHints().isEmpty() ? bestPracticeViolation.getHints().stream().map(hint -> String.format("<li>%s</li>", hint))
                                    .collect(Collectors.joining("\n", "<h4>Hints</h4><ul>", "</ul>")) : "",
                            bestPracticeViolation.getViolatedBestPractice().getWebPageHyperlink());

                    List<Action<BestPracticeViolation>> actions = Optional.ofNullable(bestPracticeViolation.getActions()).orElse(new ArrayList<>());
                    PsiElement element = bestPracticeViolation.getElement();
                    PsiFile relatedElementFile = Optional.ofNullable(bestPracticeViolation.getElement()).map(PsiElement::getContainingFile).orElse(null);
                    if (relatedElementFile != null) {
                        if (relatedElementFile == file) {
                            List<ProblemDescriptor> problemDescriptors = problemDescriptorsHashMap.computeIfAbsent(element, k -> new ArrayList<>());
                            if (problemDescriptors.stream().noneMatch(problemDescriptor -> problemDescriptionTemplate.equals(problemDescriptor.getDescriptionTemplate()))) {
                                if (bestPracticeViolation.getStartElement() != null && bestPracticeViolation.getEndElement() != null) {
                                    problemDescriptors.add(InspectionManager.getInstance(project).createProblemDescriptor(
                                            bestPracticeViolation.getStartElement(),
                                            bestPracticeViolation.getEndElement(),
                                            problemDescriptionTemplate, ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                            isOnTheFly,
                                            createQuickFixes(actions, bestPracticeViolation).toArray(new LocalQuickFix[0])));

                                } else {
                                    problemDescriptors.add(InspectionManager.getInstance(project).createProblemDescriptor(
                                            element,
                                            problemDescriptionTemplate,
                                            isOnTheFly,
                                            createQuickFixes(actions, bestPracticeViolation).toArray(new LocalQuickFix[0]),
                                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING));

                                }
                            }
                        }
                    }

                });
                problemDescriptorsHashMap.forEach((psiElement, problemDescriptors) -> problemDescriptors.forEach(holder::registerProblem));
            }

            ;
        };

    }

    private List<LocalQuickFix> createQuickFixes(List<Action<BestPracticeViolation>> actions, BestPracticeViolation bestPracticeViolation) {
        return actions.stream()
                .map(action -> {
                    return new LocalQuickFix() {
                        @Override
                        public
                        @NotNull
                        String getFamilyName() {
                            return action.getName();
                        }

                        @Override
                        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
                            action.execute(bestPracticeViolation);
                        }
                    };
                }).collect(Collectors.toList());
    }

}
