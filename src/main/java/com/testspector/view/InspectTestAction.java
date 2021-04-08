package com.testspector.view;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.testspector.controller.TestspectorController;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class InspectTestAction extends AnAction {


    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);
        Project project = anActionEvent.getProject();
        if (project != null) {
            TestspectorController testspectorController = project.getComponent(TestspectorController.class);
            if (psiFile == null) {
                VirtualFile virtualFile = anActionEvent.getData(CommonDataKeys.VIRTUAL_FILE);
                if (virtualFile != null) {
                    try {
                        List<PsiFile> psiFiles = collectPsiFilesFromVirtualFile(project, virtualFile);
                        testspectorController.initializeTestspector(psiFiles, virtualFile.getName());
                    } catch (Exception ignored) {
                    }
                }
            } else {
                testspectorController.initializeTestspector(psiFile, psiFile.getName());
            }
        }
    }

    private List<PsiFile> collectPsiFilesFromVirtualFile(Project project, VirtualFile virtualFile) throws Exception {
        return ProgressManager
                .getInstance()
                .run(new Task.WithResult<List<PsiFile>,
                        Exception>(project, "Collecting Files", true) {
                    @Override
                    protected List<PsiFile> compute(@NotNull ProgressIndicator indicator) {
                        return ApplicationManager
                                .getApplication()
                                .runReadAction((Computable<List<PsiFile>>) () ->
                                        collectPsiFilesFromVirtualFile(indicator, project, virtualFile));
                    }
                });
    }

    private List<PsiFile> collectPsiFilesFromVirtualFile(ProgressIndicator progressIndicator, Project project, VirtualFile virtualFile) {
        ArrayList<PsiFile> collectedPsiFiles = new ArrayList<>();
        progressIndicator.setText(virtualFile.getName());
        VfsUtilCore.visitChildrenRecursively(virtualFile, new VirtualFileVisitor<PsiFile>() {
            @NotNull
            @Override
            public Result visitFileEx(@NotNull VirtualFile file) {
                progressIndicator.setText(file.getName());
                PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                if (psiFile != null) {
                    collectedPsiFiles.add(psiFile);
                }
                return CONTINUE;
            }
        });
        return collectedPsiFiles;
    }

    @Override
    public void update(AnActionEvent e) {
    }


    @Override
    public boolean isDumbAware() {
        return false;
    }
}
