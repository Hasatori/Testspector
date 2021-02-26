package com.testspector.view.report;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.testspector.model.checking.BestPracticeViolation;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TreeViewReport extends JTree {

    private final Project project;

    public TreeViewReport(List<BestPracticeViolation> bestPracticeViolations, Project project) {
        super();
        this.project = project;
        HashMap<PsiElement, List<BestPracticeViolation>> psiElementBestPracticeViolationHashMap = new HashMap<>();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        for (BestPracticeViolation bestPracticeViolation : bestPracticeViolations) {
            List<BestPracticeViolation> foundGroup = psiElementBestPracticeViolationHashMap.get(bestPracticeViolation.getPsiElement());
            if (foundGroup == null) {
                foundGroup = new ArrayList<>();
            }
            foundGroup.add(bestPracticeViolation);
            psiElementBestPracticeViolationHashMap.put(bestPracticeViolation.getPsiElement(), foundGroup);
        }
        psiElementBestPracticeViolationHashMap.forEach((element, group) -> {
            if (group.size() == 1) {
                BestPracticeViolation bestPracticeViolation = group.get(0);
                PsiElement mainNavigationElement = bestPracticeViolation.getPsiElement().getNavigationElement();
                WrapperNode bestPracticeViolationNode = new WrapperNode(mainNavigationElement, bestPracticeViolation.getPsiElement().toString());
                bestPracticeViolationNode.add(new ViolatedRuleNode(mainNavigationElement, bestPracticeViolation.getViolatedRule()));
                bestPracticeViolationNode.add(new ShowHideNode(mainNavigationElement, bestPracticeViolation.getPsiElement(), bestPracticeViolation.getTextRange(), this, "Highlight problematic code", "Delete highlighting of the code"));
                bestPracticeViolationNode.add(new WarningNode(mainNavigationElement, bestPracticeViolation.getProblemDescription()));
                if (bestPracticeViolation.getHints() != null && bestPracticeViolation.getHints().size() > 0) {
                    InfoNode hintsWrapper = new InfoNode(mainNavigationElement, "Hints");
                    for (String hint : bestPracticeViolation.getHints()) {
                        SimpleTextNode simpleTextNode = new SimpleTextNode(mainNavigationElement, hint);
                        hintsWrapper.add(simpleTextNode);
                    }
                    bestPracticeViolationNode.add(hintsWrapper);
                }
                if (bestPracticeViolation.getErrorElements() != null && bestPracticeViolation.getErrorElements().size() > 0) {
                   WarningNode errorsWrapper = new WarningNode(mainNavigationElement, "Error elements");
                    for (PsiElement errorElement : bestPracticeViolation.getErrorElements()) {
                        PsiElement errorElementNavigationElement = errorElement.getNavigationElement();
                        WrapperNode errorElementWrapper = new WrapperNode(errorElementNavigationElement, errorElement.toString());
                        errorElementWrapper.add(new ShowHideNode(errorElementNavigationElement, errorElement, errorElement.getTextRange(), this, "Highlight error element", "Deleted highlighting of the element"));
                        errorsWrapper.add(errorElementWrapper);
                    }
                    bestPracticeViolationNode.add(errorsWrapper);
                }
                root.add(bestPracticeViolationNode);
            }else{
                PsiElement mainNavigationElement = element.getNavigationElement();
                WrapperNode groupNode = new WrapperNode(mainNavigationElement,element.toString());
                for (BestPracticeViolation bestPracticeViolation : group) {
                    WrapperNode bestPracticeViolationNode = new WrapperNode(mainNavigationElement,bestPracticeViolation.getViolatedRule().getDisplayName());
                    bestPracticeViolationNode.add(new ViolatedRuleNode(mainNavigationElement, bestPracticeViolation.getViolatedRule()));
                    bestPracticeViolationNode.add(new ShowHideNode(mainNavigationElement, bestPracticeViolation.getPsiElement(), bestPracticeViolation.getTextRange(), this, "Highlight problematic code", "Delete highlighting of the code"));
                    bestPracticeViolationNode.add(new WarningNode(mainNavigationElement, bestPracticeViolation.getProblemDescription()));
                    if (bestPracticeViolation.getHints() != null && bestPracticeViolation.getHints().size() > 0) {
                        InfoNode hintsWrapper = new InfoNode(mainNavigationElement, "Hints");
                        for (String hint : bestPracticeViolation.getHints()) {
                            SimpleTextNode simpleTextNode = new SimpleTextNode(mainNavigationElement, hint);
                            hintsWrapper.add(simpleTextNode);
                        }
                        bestPracticeViolationNode.add(hintsWrapper);
                    }
                    if (bestPracticeViolation.getErrorElements() != null && bestPracticeViolation.getErrorElements().size() > 0) {
                       WarningNode errorsWrapper = new WarningNode(mainNavigationElement, "Error elements");
                        for (PsiElement errorElement : bestPracticeViolation.getErrorElements()) {
                            PsiElement errorElementNavigationElement = errorElement.getNavigationElement();
                            WrapperNode errorElementWrapper = new WrapperNode(errorElementNavigationElement, errorElement.toString());
                            errorElementWrapper.add(new ShowHideNode(errorElementNavigationElement, errorElement, errorElement.getTextRange(), this, "Highlight error element", "Deleted highlighting of the element"));
                            errorsWrapper.add(errorElementWrapper);
                        }
                        bestPracticeViolationNode.add(errorsWrapper);
                    }
                    groupNode.add(bestPracticeViolationNode);
                }

                root.add(groupNode);
            }


        });
        this.setAlignmentX(LEFT_ALIGNMENT);
        this.setAlignmentY(TOP_ALIGNMENT);
        this.setModel(new DefaultTreeModel(root));
        this.setRootVisible(false);
        this.setCellRenderer(new TreeReportCellRenderer());
        this.addMouseListener(new TreeReportMouseListener());
    }

    public void highlightAll() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.getModel().getRoot();
        for (int i = 0; i < root.getChildCount(); i++) {
            TreeNode node = root.getChildAt(i);
            if (node instanceof BestPracticeViolationNode) {
                BestPracticeViolationNode bestPracticeViolationNode = (BestPracticeViolationNode) node;
                this.expandPath(new TreePath(bestPracticeViolationNode.getPath()));
                for (int j = 0; j < bestPracticeViolationNode.getChildCount(); j++) {
                    TreeNode child = bestPracticeViolationNode.getChildAt(j);
                    if (child instanceof ShowHideNode) {
                        ShowHideNode showHideNode = (ShowHideNode) child;
                        showHideNode.highlight();
                    }
                }
            }
        }
    }

    public void expandAll() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.getModel().getRoot();
        expandAllInNode(root);
    }

    private void expandAllInNode(DefaultMutableTreeNode node) {
        this.expandPath(new TreePath(node.getPath()));
        for (int i = 0; i < node.getChildCount(); i++) {
            TreeNode child = node.getChildAt(i);
            if (child instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode castedChild = (DefaultMutableTreeNode) child;
                this.expandPath(new TreePath(castedChild.getPath()));
                expandAllInNode(castedChild);
            }
        }
    }

    public void collapseAll() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.getModel().getRoot();
        collapseAllInNode(root);
    }

    private void collapseAllInNode(DefaultMutableTreeNode node) {
        this.collapsePath(new TreePath(node.getPath()));
        for (int i = 0; i < node.getChildCount(); i++) {
            TreeNode child = node.getChildAt(i);
            if (child instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode castedChild = (DefaultMutableTreeNode) child;
                this.collapsePath(new TreePath(castedChild.getPath()));
                //collapseAllInNode(castedChild);
            }
        }
    }

}
