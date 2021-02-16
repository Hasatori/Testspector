package com.testspector.view.report;

import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class TreeReportMouseListener implements MouseListener {
    @Override
    public void mouseClicked(MouseEvent e) {
        JTree root = ((JTree) e.getComponent());
        Object clickedNode = root.getSelectionModel().getSelectionPath().getLastPathComponent();
        if (clickedNode instanceof BestPracticeViolationNode) {
            BestPracticeViolationNode bestPracticeViolationNode = (BestPracticeViolationNode) clickedNode;
            PsiElement navigationElement = bestPracticeViolationNode.getBestPracticeViolation().getPsiElement().getNavigationElement();
            if (navigationElement != null && navigationElement instanceof Navigatable && ((Navigatable) navigationElement).canNavigate()) {
                ((Navigatable) navigationElement).navigate(true);
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
                Desktop.getDesktop().browse(new URI(violatedRuleNode.getBestPracticeViolation().getViolatedRule().getWebPageHyperlink()));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (URISyntaxException uriSyntaxException) {
                uriSyntaxException.printStackTrace();
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
