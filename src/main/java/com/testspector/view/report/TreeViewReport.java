package com.testspector.view.report;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.enums.BestPractice;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TreeViewReport extends JTree {

    public static final List<String> groupBy = Arrays.asList("Files", "Best practice");
    private final Project project;
    private final List<BestPracticeViolation> bestPracticeViolations;

    public TreeViewReport(List<BestPracticeViolation> bestPracticeViolations, Project project, GroupBy groupBy) {
        super();
        this.project = project;
        this.bestPracticeViolations = bestPracticeViolations;
        this.groupBy(groupBy);

        this.setAlignmentX(LEFT_ALIGNMENT);
        this.setAlignmentY(TOP_ALIGNMENT);

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

    public void groupBy(GroupBy groupBy) {
        switch (groupBy) {
            case FILES:
                this.groupByFiles();
                break;
            case BEST_PRACTICE:
                this.groupByBestPractice();
                break;
        }
    }

    private void groupByBestPractice() {
        HashMap<BestPractice, List<BestPracticeViolation>> psiElementBestPracticeViolationHashMap = new HashMap<>();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        for (BestPracticeViolation bestPracticeViolation : bestPracticeViolations) {
            List<BestPracticeViolation> foundGroup = psiElementBestPracticeViolationHashMap.get(bestPracticeViolation.getViolatedRule());
            if (foundGroup == null) {
                foundGroup = new ArrayList<>();
            }
            foundGroup.add(bestPracticeViolation);
            psiElementBestPracticeViolationHashMap.put(bestPracticeViolation.getViolatedRule(), foundGroup);
        }

        psiElementBestPracticeViolationHashMap.forEach((bestPractice, group) -> {
            ViolatedRuleNode groupNode = new ViolatedRuleNode(bestPractice);
            for (BestPracticeViolation bestPracticeViolation : group) {
                PsiElement mainNavigationElement = bestPracticeViolation.getPsiElement().getNavigationElement();
                WrapperNode bestPracticeViolationNode = new WrapperNode(mainNavigationElement, bestPracticeViolation.getPsiElement().toString());
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
        });
        this.setModel(new DefaultTreeModel(root));
    }

    private void groupByFiles() {
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
            PsiElement mainNavigationElement = element.getNavigationElement();
            WrapperNode groupNode = new WrapperNode(mainNavigationElement, element.toString());
            for (BestPracticeViolation bestPracticeViolation : group) {
                ViolatedRuleNode bestPracticeViolationNode = new ViolatedRuleNode(mainNavigationElement, bestPracticeViolation.getViolatedRule());
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
        });
        this.setModel(new DefaultTreeModel(root));
    }

    public enum GroupBy {
        FILES("Files"),
        BEST_PRACTICE("Best practice");

        private final String displayName;

        GroupBy(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return this.displayName;
        }
    }
}
