package com.testspector.view;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.testspector.controller.TestspectorController;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InspectTestAction extends AnAction {


    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) {
            VirtualFile virtualFile = anActionEvent.getData(CommonDataKeys.VIRTUAL_FILE);
            TestspectorController.initializeTestspector(collectPsiFilesFromVirtualFile(virtualFile),virtualFile.getCanonicalPath());
        } else {
            TestspectorController.initializeTestspector(psiFile);
        }
    }

    private List<PsiFile> collectPsiFilesFromVirtualFile(VirtualFile virtualFile){
        ArrayList<PsiFile> collectedPsiFiles = new ArrayList<>();
        Arrays.stream(virtualFile.getChildren()).forEach(virtualFile1 -> {
            PsiFile file = PsiManager.getInstance(ProjectManager.getInstance().getOpenProjects()[0]).findFile(virtualFile1);
            if (file != null) {
                collectedPsiFiles.add(file);
            }
            collectedPsiFiles.addAll(collectPsiFilesFromVirtualFile(virtualFile1));
        });
        return collectedPsiFiles;
    }

    @Override
    public void update(AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
    }


    @Override
    public boolean isDumbAware() {
        return false;
    }
}
