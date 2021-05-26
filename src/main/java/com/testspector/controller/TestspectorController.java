package com.testspector.controller;

import com.intellij.analysis.AnalysisScope;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ex.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class TestspectorController {

    private final Project project;

    public TestspectorController(Project project) {
        this.project = project;
    }

    public void initializeTestspector(VirtualFile virtualFile) {
        InspectionProfileImpl inspectionProfile = InspectionProjectProfileManager.getInstance(this.project).getCurrentProfile();
        List<ScopeToolState> scopeToolStates = inspectionProfile.getAllTools().stream().filter(scopeToolState -> (scopeToolState.getTool().getTool() instanceof BestPracticeInspection) && scopeToolState.isEnabled()).collect(Collectors.toList());
        FileDocumentManager.getInstance().saveAllDocuments();
        final GlobalInspectionContextImpl inspectionContext = ((InspectionManagerEx) InspectionManager.getInstance(project)).createNewGlobalContext();
        InspectionToolsSupplier inspectionToolsSupplier = new InspectionToolsSupplier.Simple(scopeToolStates.stream().map(ScopeToolState::getTool).collect(Collectors.toList()));
        inspectionProfile = new InspectionProfileImpl("Testspector", inspectionToolsSupplier, inspectionProfile);
        inspectionContext.setExternalProfile(inspectionProfile);
        AnalysisScope scope = new AnalysisScope(project, Collections.singletonList(virtualFile));
        inspectionContext.setCurrentScope(scope);
        scope.setSearchInLibraries(false);
        scope.setIncludeTestSource(true);
        inspectionContext.doInspections(scope);
    }

}
