package com.testspector.view.report;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.event.EditorMouseEvent;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.editor.impl.EditorMarkupModelImpl;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.testspector.view.CustomIcon;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;

public class ShowHideNode extends BestPracticeViolationNode {

    private final TreeViewReport treeViewReport;
    private final PsiElement element;
    private final String onHideLabel;
    private final String onShowLabel;
    private final TextRange textRange;
    private boolean isCodeHighlighted = false;
    private RangeHighlighter highlighter;

    public ShowHideNode(PsiElement navigationElement, PsiElement element, TextRange textRange, TreeViewReport treeViewReport, String onShowLabel, String onHideLabel) {
        super(navigationElement);
        this.highlighter = null;
        this.treeViewReport = treeViewReport;
        this.element = element;
        this.onShowLabel = onShowLabel;
        this.onHideLabel = onHideLabel;
        this.textRange = textRange;

    }

    public String getOnHideLabel() {
        return onHideLabel;
    }

    public String getOnShowLabel() {
        return onShowLabel;
    }

    public boolean isCodeHighlighted() {
        return isCodeHighlighted;
    }

    private void setCodeHighlighted(boolean codeHighlighted) {
        isCodeHighlighted = codeHighlighted;
    }

    public PsiElement getElement() {
        return element;
    }

    private RangeHighlighter getHighlighter() {
        return highlighter;
    }

    public void setHighlighter(RangeHighlighter highlighter) {
        this.highlighter = highlighter;
    }

    public void highlight() {
        FileEditor[] editors = FileEditorManager.getInstance(getElement().getProject()).getEditors(getElement().getContainingFile().getVirtualFile());
        MarkupModel markupModel = null;
        for (FileEditor editor2 : editors) {
            if (editor2 instanceof TextEditor) {
                markupModel = ((TextEditor) editor2).getEditor().getMarkupModel();

            }
        }
        if (markupModel != null) {
            if (!isCodeHighlighted()) {
                setCodeHighlighted(true);
                setHighlighter(createRangeHighlighter(markupModel));
                ((EditorMarkupModelImpl) markupModel).getEditor().addEditorMouseListener(new EditorMouseListener() {
                    @Override
                    public void mouseClicked(@NotNull EditorMouseEvent event) {
                        int offset = event.getEditor().getCaretModel().getCurrentCaret().getOffset();
                        if (offset <= textRange.getEndOffset() && offset >= textRange.getStartOffset()) {
                            treeViewReport.clearSelection();
                            treeViewReport.setSelectionPath(new TreePath(getPath()));
                        }
                    }
                });
                com.intellij.codeInsight.daemon.DaemonCodeAnalyzer.getInstance(getElement().getProject()).restart();
                getHighlighter().setGutterIconRenderer(createGutterIconRenderer(markupModel));

            } else {
                setCodeHighlighted(false);
                markupModel.removeHighlighter(getHighlighter());
            }

        }
    }
    private GutterIconRenderer createGutterIconRenderer(MarkupModel markupModel) {
        return new GutterIconRenderer() {
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
                return CustomIcon.DELETE.getBasic();
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
                        markupModel.removeHighlighter(getHighlighter());
                    }
                };
            }
        };
    }

    private RangeHighlighter createRangeHighlighter(MarkupModel markupModel) {
        return markupModel.addRangeHighlighter(
                textRange.getStartOffset(),
                textRange.getEndOffset(),
                9999,
                new TextAttributes(
                        null,
                        null,
                        Color.BLUE,
                        EffectType.BOLD_LINE_UNDERSCORE,
                        Font.PLAIN),
                HighlighterTargetArea.EXACT_RANGE);
    }
}
