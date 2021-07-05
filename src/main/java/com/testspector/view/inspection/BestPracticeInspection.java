package com.testspector.view.inspection;


import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.testspector.controller.TestspectorController;
import com.testspector.model.checking.Action;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.enums.BestPractice;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public abstract class BestPracticeInspection extends LocalInspectionTool {

    @NotNull
    public abstract BestPractice getBestPractice();

    @Override
    public boolean isEnabledByDefault() {
        return true;
    }

    @NotNull
    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence)
    String [] getGroupPath() {
        return new String[]{"Testspector"};
    }
    @Override
    public @NonNls
    @Nullable
    String getGroupKey() {
        return null;
    }

    @Override
    public @NonNls
    @NotNull
    String getShortName() {
        return getBestPractice().name();
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    String getDisplayName() {
        return getBestPractice().getDisplayName();
    }

    @Override
    public @NotNull
    PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {

        return new PsiElementVisitor() {

            @Override
            public void visitFile(@NotNull PsiFile file) {
                Project project = file.getProject();
                List<BestPracticeViolation> bestPracticeViolations = project.getService(TestspectorController.class).inspectFile(file, getBestPractice(), session);
                HashMap<PsiElement, List<ProblemDescriptor>> problemDescriptorsHashMap = new HashMap<>();
                bestPracticeViolations.forEach(bestPracticeViolation -> {
                    String problemDescriptionTemplate = String.format(
                                    "<html><p>%s</p>" +
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
