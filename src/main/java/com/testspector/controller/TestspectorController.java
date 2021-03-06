package com.testspector.controller;

import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.testspector.model.checking.BestPracticeCheckingStrategy;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.InspectionInvocationLineResolveStrategy;
import com.testspector.model.checking.TestLineCrate;
import com.testspector.model.checking.factory.*;
import com.testspector.model.enums.ProgrammingLanguage;
import com.testspector.model.enums.UnitTestFramework;
import com.testspector.view.ToolWindowContent;
import com.testspector.view.ToolWindowContentFactory;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public final class TestspectorController {

    private static final String TOOL_WINDOW_NAME = "Testspector";
    private final Project project;

    public TestspectorController(Project project) {
        this.project = project;
    }

    public void initializeTestspector(PsiElement element, String name) {
        initializeTestspector(Collections.singletonList(element), name);
    }

    public void initializeTestspector(List<? extends PsiElement> files, String name) {
        ToolWindowContent toolWindowContent = project.getComponent(ToolWindowContentFactory.class).getToolWindowContent((toolWindowContent1) -> {
            toolWindowContent1.getConsoleView().print("\nRerunning inspection on " + name, ConsoleViewContentType.LOG_INFO_OUTPUT);
            initializeInspection(files, toolWindowContent1);
        });
        initializeInspection(files, toolWindowContent);
        addTabToToolWindow(toolWindowContent, name);
    }

    public Optional<TestLineCrate> resolveTestLineCrate(PsiElement element) {
        Optional<ProgrammingLanguage> optionalProgrammingLanguage = project.getComponent(ProgrammingLanguageFactory.class).getProgrammingLanguage(element);
        if (optionalProgrammingLanguage.isPresent()) {
            List<UnitTestFramework> unitTestFrameworks = project
                    .getComponent(UnitTestFrameworkFactoryProvider.class)
                    .geUnitTestFrameworkFactory(optionalProgrammingLanguage.get())
                    .stream()
                    .map(unitTestFrameworkFactory -> unitTestFrameworkFactory.getUnitTestFramework(element))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            if (!unitTestFrameworks.isEmpty()) {
                UnitTestFramework unitTestFramework = unitTestFrameworks.get(0);
                Optional<InspectionInvocationLineResolveStrategy> optionalUnitTestLineResolveStrategy = project
                        .getComponent(InspectionInvocationLineResolveStrategyFactory.class)
                        .getInspectionInvocationLineResolveStrategy(unitTestFramework);
                if (optionalUnitTestLineResolveStrategy.isPresent()) {
                    Optional<PsiElement> optionalLineElement = optionalUnitTestLineResolveStrategy.get().resolveInspectionInvocationLine(element);
                    if (optionalLineElement.isPresent()) {
                        return Optional.of(new TestLineCrate(optionalLineElement.get(), optionalProgrammingLanguage.get(), unitTestFramework));
                    }
                }
            }
        }
        return Optional.empty();
    }

    private void initializeInspection(List<? extends PsiElement> elements, ToolWindowContent toolWindowContent) {
        ExecutorService executorService = project.getComponent(TestSpectorExecutorServiceFactory.class)
                .getTestSpectorExecutorService(10);
        ProgressManager.getInstance().run(new Task.Modal(project, "Inspecting Unit Tests", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                List<Callable<List<BestPracticeViolation>>> callables = prepareCallables(elements, indicator);
                List<BestPracticeViolation> bestPracticeViolations = Collections.synchronizedList(new ArrayList<>());
                try {
                    bestPracticeViolations.addAll(fillBestPractices(callables, executorService));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                awaitTerminationAfterShutdown(executorService);
                ApplicationManager.getApplication().runReadAction(() -> {
                    if (indicator.isCanceled()) {
                        toolWindowContent.cancel();
                    } else {
                        toolWindowContent.setData(bestPracticeViolations);
                    }
                });
            }
        });

    }

    private List<BestPracticeViolation> fillBestPractices(List<Callable<List<BestPracticeViolation>>> callables, ExecutorService executorService) throws InterruptedException {
        List<BestPracticeViolation> bestPracticeViolations = Collections.synchronizedList(new ArrayList<>());
        bestPracticeViolations.addAll(executorService.invokeAll(callables)
                .stream()
                .map(listFuture -> {
                    try {
                        return listFuture.get();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }).filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toList()));
        return bestPracticeViolations;
    }

    private List<Callable<List<BestPracticeViolation>>> prepareCallables(List<? extends PsiElement> elements, ProgressIndicator indicator) {
        List<Callable<List<BestPracticeViolation>>> callables = new ArrayList();
        double max = elements.size();
        AtomicReference<Double> index = new AtomicReference<>((double) 0);
        for (PsiElement element : elements) {
            callables.add(() -> ApplicationManager
                    .getApplication()
                    .runReadAction((Computable<List<BestPracticeViolation>>) () -> {
                        if (!indicator.isCanceled()) {
                            index.set(index.get() + 1);
                            String name = null;
                            if (element instanceof PsiFile) {
                                name = ((PsiFile) element).getName();
                            } else {
                                name = element.toString();
                            }
                            indicator.setIndeterminate(false);
                            indicator.setText("Inspecting " + name);
                            indicator.setFraction(index.get() / max);
                            return gatherFromElement(element, indicator);
                        } else {
                            indicator.setText("Cancelling...");
                            indicator.setIndeterminate(true);
                            return null;
                        }
                    }));
        }
        return callables;
    }

    private void awaitTerminationAfterShutdown(ExecutorService threadPool) {
        threadPool.shutdownNow();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private List<BestPracticeViolation> gatherFromElement(PsiElement element, ProgressIndicator indicator) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        if (!indicator.isCanceled()) {
            Optional<ProgrammingLanguage> optionalProgrammingLanguage = project.getComponent(ProgrammingLanguageFactory.class)
                    .getProgrammingLanguage(element);
            if (optionalProgrammingLanguage.isPresent()) {
                List<UnitTestFramework> unitTestFrameworks = project
                        .getComponent(UnitTestFrameworkFactoryProvider.class)
                        .geUnitTestFrameworkFactory(optionalProgrammingLanguage.get())
                        .stream()
                        .map(unitTestFrameworkFactory -> unitTestFrameworkFactory.getUnitTestFramework(element))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
                if (unitTestFrameworks.size() > 0) {
                    for (UnitTestFramework unitTestFramework : unitTestFrameworks) {
                        Optional<BestPracticeCheckingStrategy<PsiElement>> optionalBestPracticeCheckingStrategy = project
                                .getComponent(BestPracticeCheckingStrategyFactoryProvider.class)
                                .getBestPracticeCheckingStrategyFactory(optionalProgrammingLanguage.get(), unitTestFramework)
                                .map(BestPracticeCheckingStrategyFactory::getBestPracticeCheckingStrategy);
                        if (optionalBestPracticeCheckingStrategy.isPresent()) {
                            List<BestPracticeViolation> foundViolations = optionalBestPracticeCheckingStrategy.get().checkBestPractices(element);
                            bestPracticeViolations.addAll(foundViolations);
                        }
                    }
                }
            }
            return bestPracticeViolations;
        } else {
            indicator.setText("Cancelling...");
            indicator.setIndeterminate(true);
        }
        return bestPracticeViolations;
    }

    private void addTabToToolWindow(ToolWindowContent toolWindowContent, String tabName) {
        ToolWindow toolWindow = ToolWindowManager
                .getInstance(project)
                .getToolWindow(TOOL_WINDOW_NAME);
        if (toolWindow == null) {
            toolWindow = ToolWindowManager
                    .getInstance(project)
                    .registerToolWindow(TOOL_WINDOW_NAME, true, ToolWindowAnchor.BOTTOM);
        }
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(toolWindowContent.getPanel1(), tabName, false);
        toolWindow.getContentManager().addContent(content);
        if (toolWindow.getContentManager().getContentCount() > 1) {
            toolWindow.getContentManager().selectNextContent();
        }
    }
}
