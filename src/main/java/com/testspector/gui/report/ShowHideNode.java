package com.testspector.gui.report;

import com.intellij.codeInsight.daemon.impl.AnnotationHolderImpl;
import com.intellij.lang.annotation.AnnotationSession;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.editor.impl.EditorMarkupModelImpl;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.testspector.checking.BestPracticeViolation;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;

public class ShowHideNode extends BestPracticeViolationNode {

    private final TextRange textRange;
    private final PsiElement psiElement;
    private final Project project;
    private final TreeViewReport treeViewReport;
    private boolean isCodeHighlighted = false;
    private RangeHighlighter highlighter;
    private String text = "Show highliting";

    public ShowHideNode(BestPracticeViolation bestPracticeViolation, Project project, TreeViewReport treeViewReport) {
        super(bestPracticeViolation);
        this.textRange = bestPracticeViolation.getTextRange();
        this.psiElement = bestPracticeViolation.getPsiElement();
        this.highlighter = null;
        this.project = project;
        this.treeViewReport = treeViewReport;
    }

    public boolean isCodeHighlighted() {
        return isCodeHighlighted;
    }

    public void setCodeHighlighted(boolean codeHighlighted) {
        isCodeHighlighted = codeHighlighted;
    }

    public PsiElement getPsiElement() {
        return psiElement;
    }

    public TextRange getTextRange() {
        return textRange;
    }

    public RangeHighlighter getHighlighter() {
        return highlighter;
    }

    public void setHighlighter(RangeHighlighter highlighter) {
        this.highlighter = highlighter;
    }

    public void highlight() {
        FileEditor[] editors = FileEditorManager.getInstance(project).getEditors(getPsiElement().getContainingFile().getVirtualFile());
        MarkupModel markupModel = null;

        for (FileEditor editor2 : editors) {
            if (editor2 instanceof TextEditor) {
                markupModel = ((TextEditor) editor2).getEditor().getMarkupModel();

            }
        }
        if (markupModel != null) {
            if (!isCodeHighlighted()) {
                setCodeHighlighted(true);
                setHighlighter(markupModel.addRangeHighlighter(getTextRange().getStartOffset(), getTextRange().getEndOffset(), 9999, new TextAttributes(null, null, Color.BLUE, EffectType.BOLD_LINE_UNDERSCORE, Font.PLAIN), HighlighterTargetArea.EXACT_RANGE));
                ((EditorMarkupModelImpl) markupModel).getEditor().addEditorMouseListener(new EditorMouseListener() {
                    @Override
                    public void mouseClicked(@NotNull EditorMouseEvent event) {
                        int offset = event.getEditor().getCaretModel().getCurrentCaret().getOffset();
                        if (offset <= getTextRange().getEndOffset() && offset >= getTextRange().getStartOffset()) {
                            treeViewReport.clearSelection();
                            treeViewReport.setSelectionPath(new TreePath(getPath()));
                        }
                    }
                });
                MarkupModel finalMarkupModel = markupModel;
                new AnnotationHolderImpl(new AnnotationSession(getPsiElement().getContainingFile())).createAnnotation(HighlightSeverity.INFORMATION, getTextRange(), "Test");
                com.intellij.codeInsight.daemon.DaemonCodeAnalyzer.getInstance(project).restart();
                getHighlighter().setGutterIconRenderer(new GutterIconRenderer() {
                    @Override
                    public boolean equals(Object obj) {
                        return false;
                    }

                    @Override
                    public int hashCode() {
                        return 0;
                    }

                    @NotNull
                    @Override
                    public Icon getIcon() {
                        return IconLoader.getIcon("/icons/deleteTag_dark.svg");
                    }

                    @Override
                    public String getTooltipText() {
                        return "Hide";
                    }

                    @Override
                    public Alignment getAlignment() {
                        return Alignment.RIGHT;


                    }

                    @Override
                    public AnAction getClickAction() {
                        return new AnAction() {
                            @Override
                            public void actionPerformed(@NotNull AnActionEvent e) {
                                setCodeHighlighted(false);
                                finalMarkupModel.removeHighlighter(getHighlighter());
                            }
                        };

                    }
                });

            } else {
                setCodeHighlighted(false);
                markupModel.removeHighlighter(getHighlighter());
            }

        }
    }
}
