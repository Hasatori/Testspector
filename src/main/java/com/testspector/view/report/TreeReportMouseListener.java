package com.testspector.view.report;

import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class TreeReportMouseListener implements MouseListener {
    @Override
    public void mouseClicked(MouseEvent e) {
        JTree root = ((JTree) e.getComponent());
        TreePath selectionPath = root.getSelectionModel().getSelectionPath();
        if (selectionPath != null) {
            Object clickedNode = selectionPath.getLastPathComponent();
            if (clickedNode instanceof BestPracticeViolationNode) {
                BestPracticeViolationNode bestPracticeViolationNode = (BestPracticeViolationNode) clickedNode;
                Optional<PsiElement> optionalNavigationElement = bestPracticeViolationNode.getNavigationElement();
                if (optionalNavigationElement.isPresent() && optionalNavigationElement.get() instanceof Navigatable && ((Navigatable) optionalNavigationElement.get()).canNavigate()) {
                    ((Navigatable) optionalNavigationElement.get()).navigate(true);
                }
            }

            if (clickedNode instanceof ShowHideNode) {
                ShowHideNode showHideNode = (ShowHideNode) clickedNode;
                showHideNode.highlight();
                root.getSelectionModel().clearSelection();
            }

            if (clickedNode instanceof ViolatedRuleNode) {
                ViolatedRuleNode violatedRuleNode = (ViolatedRuleNode) clickedNode;
                try {
                    Desktop.getDesktop().browse(new URI(violatedRuleNode.getViolatedBestPractice().getWebPageHyperlink()));
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } catch (URISyntaxException uriSyntaxException) {
                    uriSyntaxException.printStackTrace();
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
