package com.testspector.controller;


import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
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

    protected static HashMap<BestPractice, HashMap<PsiFile, List<ProblemDescriptor>>> fileListHashMap = new HashMap<>();

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
                List<ProblemDescriptor> registeredProblems = fileListHashMap.get(getBestPractice()).get(file);
                if (registeredProblems != null) {
                    List<ProblemDescriptor> nullElements = registeredProblems.stream().filter(problem -> problem.getPsiElement() == null).collect(Collectors.toList());
                    registeredProblems.removeAll(nullElements);
                    registeredProblems.forEach(holder::registerProblem);
                }
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
                                    "<h3>Rule <strong>%s</strong> was violated</h3>" +
                                    "<h4>Description</h4>" +
                                    "<p>%s</p>" +
                                    "%s" +
                                    "<br/>" +
                                    "<a href=\"%s\">%s</a></html>",
                            bestPracticeViolation.getViolatedBestPractice().getDisplayName(),
                            bestPracticeViolation.getProblemDescription(),
                            Optional.ofNullable(bestPracticeViolation.getHints()).isPresent() && !bestPracticeViolation.getHints().isEmpty()? bestPracticeViolation.getHints().stream().map(hint -> String.format("<li>%s</li>", hint))
                                    .collect(Collectors.joining("\n", "<h4>Hints how to solve the problem</h4><ul>", "</ul>")):"",
                            bestPracticeViolation.getViolatedBestPractice().getWebPageHyperlink(),
                            "Get more information about the rule");

                    List<LocalQuickFix> localQuickFixes = Optional.ofNullable(bestPracticeViolation.getQuickFixes()).orElse(new ArrayList<>());
                    for (PsiElement relatedElement : bestPracticeViolation.getRelatedElements()) {
                        PsiFile relatedElementFile = relatedElement.getContainingFile();
                        if (relatedElementFile == file) {
                            List<ProblemDescriptor> problemDescriptors = problemDescriptorsHashMap.computeIfAbsent(relatedElement, k -> new ArrayList<>());
                            if (problemDescriptors.stream().noneMatch(problemDescriptor -> problemDescriptionTemplate.equals(problemDescriptor.getDescriptionTemplate()))) {
                                problemDescriptors.add(InspectionManager.getInstance(project).createProblemDescriptor(relatedElement, problemDescriptionTemplate, isOnTheFly, localQuickFixes.toArray(new LocalQuickFix[0]), ProblemHighlightType.GENERIC_ERROR_OR_WARNING));
                            }
                        } else {
                            List<ProblemDescriptor> relatedElementRegisteredProblems = fileListHashMap.get(getBestPractice()).computeIfAbsent(relatedElementFile, k -> new ArrayList<>());
                            if (relatedElementRegisteredProblems.stream().noneMatch(registeredProblem -> registeredProblem.getPsiElement() == relatedElement)) {
                                relatedElementRegisteredProblems.add(InspectionManager.getInstance(project).createProblemDescriptor(relatedElement, problemDescriptionTemplate, isOnTheFly, localQuickFixes.toArray(new LocalQuickFix[0]), ProblemHighlightType.GENERIC_ERROR_OR_WARNING));

                            }
                        }
                    }

                });
                problemDescriptorsHashMap.forEach((psiElement, problemDescriptors) -> problemDescriptors.forEach(holder::registerProblem));
            }

            ;
        };
    }
}
