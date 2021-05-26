package com.testspector.view;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.testspector.controller.TestspectorController;
import org.jetbrains.annotations.NotNull;

public class InspectTestAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        if (project != null) {
            VirtualFile virtualFile = anActionEvent.getData(CommonDataKeys.VIRTUAL_FILE);
            if (virtualFile != null) {
                TestspectorController testspectorController = project.getService(TestspectorController.class);
                testspectorController.initializeTestspector(virtualFile);
            }
        }
    }

    @Override
    public void update(AnActionEvent e) {
    }

    @Override
    public boolean isDumbAware() {
        return false;
    }


}
