package com.testspector.view;

import com.intellij.openapi.project.Project;

public class ToolWindowContentFactory {

    private final Project project;

    public ToolWindowContentFactory(Project project) {
        this.project = project;
    }


    public ToolWindowContent getToolWindowContent(RerunToolWindowContentAction rerunToolWindowContentAction) {
        return new ToolWindowContent(project,rerunToolWindowContentAction);
    }

}
