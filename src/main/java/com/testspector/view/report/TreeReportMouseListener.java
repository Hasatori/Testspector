package com.testspector.view.report;

import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.testspector.view.ToolWindowContent;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Optional;

public class TreeReportMouseListener implements MouseListener {


    private final ToolWindowContent toolWindowContent;

    public TreeReportMouseListener(ToolWindowContent toolWindowContent) {
        this.toolWindowContent = toolWindowContent;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        JTree root = ((JTree) e.getComponent());
        TreePath selectionPath = root.getSelectionModel().getSelectionPath();
        if (selectionPath != null) {
            Object clickedNode = selectionPath.getLastPathComponent();
            if (clickedNode instanceof BestPracticeViolationNode) {
                BestPracticeViolationNode bestPracticeViolationNode = (BestPracticeViolationNode) clickedNode;
                Optional<PsiElement> optionalNavigationElement = bestPracticeViolationNode.getNavigationElement();
                if (optionalNavigationElement.isPresent() && optionalNavigationElement.get() instanceof Navigatable
                        && ((Navigatable) optionalNavigationElement.get()).canNavigate()) {
                    ((Navigatable) optionalNavigationElement.get()).navigate(true);
                }
                if (bestPracticeViolationNode.bestPracticeViolation != null) {
                    toolWindowContent.violationOpened(bestPracticeViolationNode.bestPracticeViolation);
                } else {
                    toolWindowContent.leaveViolationDetail();
                }
            }
            if (clickedNode instanceof ShowHideNode) {
                ShowHideNode showHideNode = (ShowHideNode) clickedNode;
                showHideNode.highlight();
                root.getSelectionModel().clearSelection();
            }

            if (clickedNode instanceof LinkNode) {
                LinkNode linkNode = (LinkNode) clickedNode;
                try {
                    Desktop.getDesktop().browse(linkNode.getUri().normalize());
                    root.getSelectionModel().clearSelection();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }


}
