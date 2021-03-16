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
import com.testspector.view.CustomIcon;
import com.testspector.view.ToolWindowContent;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public final class TestspectorController {

    private static final InspectionInvocationLineResolveStrategyFactory INSPECTION_INVOCATION_LINE_RESOLVE_STRATEGY_FACTORY = new InspectionInvocationLineResolveStrategyFactory();
    private static final UnitTestFrameworkFactoryProvider UNIT_TEST_FRAMEWORK_FACTORY_PROVIDER = new UnitTestFrameworkFactoryProvider();
    private static final BestPracticeCheckingStrategyFactoryProvider BEST_PRACTICE_CHECKING_STRATEGY_PROVIDER = new BestPracticeCheckingStrategyFactoryProvider();
    private static final SimpleDateFormat LOG_MESSAGE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final ProgrammingLanguageFactory PROGRAMMING_LANGUAGE_FACTORY = new ProgrammingLanguageFactory();
    private static final String TOOL_WINDOW_NAME = "Testspector";
    private static final HashMap<Project, ToolWindow> PROJECT_TOOL_WINDOW_HASH_MAP = new HashMap<>();

    private TestspectorController() {
    }

    public static void initializeTestspector(Project project, PsiElement element, String name) {
        initializeTestspector(project, Collections.singletonList(element), name);
    }

    public static void initializeTestspector(Project project, List<? extends PsiElement> files, String name) {
        ToolWindowContent toolWindowContent = new ToolWindowContent(project, (toolWindowContent1) -> {
            toolWindowContent1.getConsoleView().print("\nRerunning inspection on " + name, ConsoleViewContentType.LOG_INFO_OUTPUT);
            initializeTestspector(project, files, name, toolWindowContent1);
        });
        initializeTestspector(project, files, name, toolWindowContent);
    }

    private static void initializeTestspector(Project project, List<? extends PsiElement> files, String name, ToolWindowContent toolWindowContent) {
        try {
            initializeInspection(project, files, toolWindowContent);
            ToolWindow toolWindow = getToolWindow(project);
            addTabToToolWindow(toolWindow, toolWindowContent, name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Optional<TestLineCrate> resolveTestLineCrate(PsiElement element) {
        Optional<ProgrammingLanguage> optionalProgrammingLanguage = PROGRAMMING_LANGUAGE_FACTORY.getProgrammingLanguage(element);
        if (optionalProgrammingLanguage.isPresent()) {
            List<UnitTestFramework> unitTestFrameworks = UNIT_TEST_FRAMEWORK_FACTORY_PROVIDER.geUnitTestFrameworkFactory(optionalProgrammingLanguage.get())
                    .stream()
                    .map(unitTestFrameworkFactory -> unitTestFrameworkFactory.getUnitTestFramework(element))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
            if (!unitTestFrameworks.isEmpty()) {
                UnitTestFramework unitTestFramework = unitTestFrameworks.get(0);
                Optional<InspectionInvocationLineResolveStrategy> optionalUnitTestLineResolveStrategy = INSPECTION_INVOCATION_LINE_RESOLVE_STRATEGY_FACTORY.getInspectionInvocationLineResolveStrategy(unitTestFramework);
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

    private static void initializeInspection(Project project, List<? extends PsiElement> elements, ToolWindowContent toolWindowContent) {
        List<Callable<List<BestPracticeViolation>>> callables = new ArrayList();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        ProgressManager.getInstance().run(new Task.Modal(project, "Inspecting Unit Tests", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
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
                List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
                try {
                    bestPracticeViolations.addAll(executorService.invokeAll(callables, 5, TimeUnit.MINUTES)
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

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                awaitTerminationAfterShutdown(executorService);
                ApplicationManager.getApplication().runReadAction(()->{
                    if (indicator.isCanceled()) {
                        toolWindowContent.cancel();
                    } else {
                        toolWindowContent.addData(bestPracticeViolations);
                    }
                });
            }
        });

    }

    private static void awaitTerminationAfterShutdown(ExecutorService threadPool) {
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

    private static List<BestPracticeViolation> gatherFromElement(PsiElement element, ProgressIndicator indicator) {
        List<BestPracticeViolation> bestPracticeViolations = new ArrayList<>();
        if (!indicator.isCanceled()) {
            ProgressManager.getInstance().runInReadActionWithWriteActionPriority(() -> {
                Optional<ProgrammingLanguage> optionalProgrammingLanguage = PROGRAMMING_LANGUAGE_FACTORY.getProgrammingLanguage(element);
                if (optionalProgrammingLanguage.isPresent()) {
                    List<UnitTestFramework> unitTestFrameworks = UNIT_TEST_FRAMEWORK_FACTORY_PROVIDER.geUnitTestFrameworkFactory(optionalProgrammingLanguage.get())
                            .stream()
                            .map(unitTestFrameworkFactory -> unitTestFrameworkFactory.getUnitTestFramework(element))
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
                    if (unitTestFrameworks != null && unitTestFrameworks.size() > 0) {
                        for (UnitTestFramework unitTestFramework : unitTestFrameworks) {
                            Optional<BestPracticeCheckingStrategy<PsiElement>> optionalBestPracticeCheckingStrategy = BEST_PRACTICE_CHECKING_STRATEGY_PROVIDER
                                    .getBestPracticeCheckingStrategyFactory(optionalProgrammingLanguage.get(), unitTestFramework)
                                    .map(BestPracticeCheckingStrategyFactory::getBestPracticeCheckingStrategy);
                            if (optionalBestPracticeCheckingStrategy.isPresent()) {
                                List<BestPracticeViolation> foundViolations = optionalBestPracticeCheckingStrategy.get().checkBestPractices(element);
                                bestPracticeViolations.addAll(foundViolations);
                            }
                        }
                    }
                }
            }, null);
            return bestPracticeViolations;
        } else {
            indicator.setText("Cancelling...");
            indicator.setIndeterminate(true);
        }
        return bestPracticeViolations;
    }

    private static void addTabToToolWindow(ToolWindow toolWindow, ToolWindowContent toolWindowContent, String tabName) {
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(toolWindowContent.getPanel1(), tabName, false);
        toolWindow.getContentManager().addContent(content);
        if (toolWindow.getContentManager().getContentCount() > 1) {
            toolWindow.getContentManager().selectNextContent();
        }
    }

    private static ToolWindow getToolWindow(Project project) {
        ToolWindow resultToolWindow = PROJECT_TOOL_WINDOW_HASH_MAP.get(project);
        if (resultToolWindow == null) {
            resultToolWindow = ToolWindowManager.getInstance(project)
                    .registerToolWindow(TOOL_WINDOW_NAME, true, ToolWindowAnchor.BOTTOM);
            resultToolWindow.setIcon(CustomIcon.LOGO_BOTTOM_TOOL_BOX.getBasic());
            PROJECT_TOOL_WINDOW_HASH_MAP.put(project, resultToolWindow);
        }
        return resultToolWindow;
    }

    private static String getLoggingFormatMessage(String message) {
        return String.format("\n[ %s ] %s", LOG_MESSAGE_FORMAT.format(new Date()), message);
    }
}
