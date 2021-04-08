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
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public final class TestspectorController {

    private static final String TOOL_WINDOW_NAME = "Testspector";
    private static final String CANCELING_MESSAGE = "Cancelling...";
    private static final String RERUNNING_MESSAGE = "Rerunning inspection in";
    private static final String INSPECTION_LOADING_MAIN_HEADING = "Inspecting Unit Tests in";
    private static final String CREATING_REPORT_MESSAGE = "Creating report...";
    private final Project project;

    public TestspectorController(Project project) {
        this.project = project;
    }

    public void initializeTestspector(PsiElement element, String name) {
        initializeTestspector(Collections.singletonList(element), name);
    }

    public void initializeTestspector(List<? extends PsiElement> files, String name) {
        ToolWindowContent toolWindowContent = new ToolWindowContent(project, (toolWindowContent1) -> {
            toolWindowContent1.getConsoleView().print(String.format("\n%s %s", RERUNNING_MESSAGE, name), ConsoleViewContentType.LOG_INFO_OUTPUT);
            initializeInspection(files, toolWindowContent1, name);
        });
        initializeInspection(files, toolWindowContent, name);
        addTabToToolWindow(toolWindowContent, name);
    }

    public Optional<TestLineCrate> resolveTestLineCrate(PsiElement element) {
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
            if (!unitTestFrameworks.isEmpty()) {
                UnitTestFramework unitTestFramework = unitTestFrameworks.get(0);
                Optional<InspectionInvocationLineResolveStrategy> optionalUnitTestLineResolveStrategy = project
                        .getComponent(InspectionInvocationLineResolveStrategyFactory.class)
                        .getInspectionInvocationLineResolveStrategy(unitTestFramework);
                if (optionalUnitTestLineResolveStrategy.isPresent()) {
                    Optional<PsiElement> optionalLineElement = optionalUnitTestLineResolveStrategy.get()
                            .resolveInspectionInvocationLine(element);
                    if (optionalLineElement.isPresent()) {
                        return Optional.of(new TestLineCrate(
                                optionalLineElement.get(),
                                optionalProgrammingLanguage.get(),
                                unitTestFramework));
                    }
                }
            }
        }
        return Optional.empty();
    }

    private void initializeInspection(List<? extends PsiElement> elements, ToolWindowContent toolWindowContent, String name) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        ProgressManager.getInstance().run(new Task.Modal(project, String.format("%s %s", INSPECTION_LOADING_MAIN_HEADING, name), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                List<BestPracticeViolation> bestPracticeViolations = Collections.synchronizedList(new ArrayList<>());
                AtomicBoolean done = new AtomicBoolean(false);
                new Thread(() -> {
                    List<Callable<List<BestPracticeViolation>>> callables = prepareCallables(elements, indicator);
                    try {
                        bestPracticeViolations.addAll(fillBestPractices(callables, executorService));
                        done.set(true);
                    } catch (InterruptedException e) {
                        done.set(true);
                    }
                }).start();
                while (!executorService.isTerminated() && !indicator.isCanceled()){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) { }
                    if (done.get()){
                        break;
                    }
                }
                ApplicationManager.getApplication().runReadAction(() -> {
                    if (indicator.isCanceled()) {
                        indicator.setText(CANCELING_MESSAGE);
                        toolWindowContent.cancel();
                    } else {
                        indicator.setText(CREATING_REPORT_MESSAGE);
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
        AtomicInteger violationsCount = new AtomicInteger(0);
        for (PsiElement element : elements) {
            callables.add(() -> ApplicationManager
                    .getApplication()
                    .runReadAction((Computable<List<BestPracticeViolation>>) () -> {
                        if (!indicator.isCanceled()) {
                            index.set(index.get() + 1);
                            indicator.setText(getProgressMessage(violationsCount.get(), element));
                            indicator.setIndeterminate(false);
                            indicator.setFraction(index.get() / max);
                            List<BestPracticeViolation> foundViolations = gatherFromElement(element, indicator);
                            violationsCount.set(violationsCount.get() + foundViolations.size());
                            return foundViolations;
                        } else {
                            indicator.setText(CANCELING_MESSAGE);
                            indicator.setIndeterminate(true);
                            return null;
                        }
                    }));
        }
        return callables;
    }

    private String getProgressMessage(Integer violationsCount, PsiElement element) {
        String name = null;
        if (element instanceof PsiFile) {
            name = ((PsiFile) element).getName();
        } else {
            name = element.toString();
        }
        return String.format("%d violations found so far - Inspecting %s", violationsCount, name);
    }

    private List<BestPracticeViolation> gatherFromElement(PsiElement element, ProgressIndicator indicator) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        if (!indicator.isCanceled()) {
            ProgressManager.getInstance().runInReadActionWithWriteActionPriority(() -> {
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
                                List<BestPracticeViolation> foundViolations = optionalBestPracticeCheckingStrategy
                                        .get()
                                        .checkBestPractices(element);
                                bestPracticeViolations.addAll(foundViolations);
                            }
                        }
                    }
                }
            }, null);
            return bestPracticeViolations;
        } else {
            indicator.setText(CANCELING_MESSAGE);
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
        toolWindow.show(()->{});
    }
}
