package com.testspector.view.report;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.RelatedElementWrapper;
import com.testspector.model.enums.BestPractice;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

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
            try {
                groupNode.add(new LinkNode(new URI(bestPractice.getWebPageHyperlink()), "get more information about the rule"));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            for (BestPracticeViolation bestPracticeViolation : group) {
                PsiElement mainNavigationElement = bestPracticeViolation.getTestMethodElement().getNavigationElement();
                WrapperNode bestPracticeViolationNode = new WrapperNode(mainNavigationElement, bestPracticeViolation.getName());
                bestPracticeViolationNode.add(new ShowHideNode(mainNavigationElement, bestPracticeViolation.getTestMethodElement(), bestPracticeViolation.getTestMethodTextRange(), this, "Highlight", "Delete highlighting"));
                bestPracticeViolationNode.add(new WarningNode(mainNavigationElement, bestPracticeViolation.getProblemDescription()));
                if (bestPracticeViolation.getHints() != null && bestPracticeViolation.getHints().size() > 0) {
                    InfoNode hintsWrapper = new InfoNode(mainNavigationElement, "Hints");
                    for (String hint : bestPracticeViolation.getHints()) {
                        SimpleTextNode simpleTextNode = new SimpleTextNode(mainNavigationElement, hint);
                        hintsWrapper.add(simpleTextNode);
                    }
                    bestPracticeViolationNode.add(hintsWrapper);
                }
                if (bestPracticeViolation.getRelatedElements() != null && bestPracticeViolation.getRelatedElements().size() > 0) {
                    WarningNode errorsWrapper = new WarningNode(mainNavigationElement, "Related elements");
                    for (RelatedElementWrapper relatedElementWrapper : bestPracticeViolation.getRelatedElements()) {
                        WrapperNode errorElementWrapper = new WrapperNode(mainNavigationElement, relatedElementWrapper.getName());
                        for (Map.Entry<PsiElement, String> entry : relatedElementWrapper.getRelatedElementNameHashMap().entrySet()) {
                            errorElementWrapper.add(new ShowHideNode(entry.getKey().getNavigationElement(),entry.getKey(), entry.getKey().getTextRange(), this, entry.getValue() +" - highlight", entry.getValue() +" - delete highlight"));
                        }
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
            List<BestPracticeViolation> foundGroup = psiElementBestPracticeViolationHashMap.get(bestPracticeViolation.getTestMethodElement());
            if (foundGroup == null) {
                foundGroup = new ArrayList<>();
            }
            foundGroup.add(bestPracticeViolation);
            psiElementBestPracticeViolationHashMap.put(bestPracticeViolation.getTestMethodElement(), foundGroup);
        }
        psiElementBestPracticeViolationHashMap.forEach((element, group) -> {
            PsiElement mainNavigationElement = element.getNavigationElement();
            WrapperNode groupNode = new WrapperNode(mainNavigationElement, element.toString());
            for (BestPracticeViolation bestPracticeViolation : group) {
                ViolatedRuleNode bestPracticeViolationNode = new ViolatedRuleNode(mainNavigationElement, bestPracticeViolation.getViolatedRule());
                try {
                    bestPracticeViolationNode.add(new LinkNode(new URI(bestPracticeViolation.getViolatedRule().getWebPageHyperlink()), "get more information about the rule"));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                bestPracticeViolationNode.add(new ShowHideNode(mainNavigationElement, bestPracticeViolation.getTestMethodElement(), bestPracticeViolation.getTestMethodTextRange(), this, "Highlight", "Delete highlighting"));
                bestPracticeViolationNode.add(new WarningNode(mainNavigationElement, bestPracticeViolation.getProblemDescription()));
                if (bestPracticeViolation.getHints() != null && bestPracticeViolation.getHints().size() > 0) {
                    InfoNode hintsWrapper = new InfoNode(mainNavigationElement, "Hints");
                    for (String hint : bestPracticeViolation.getHints()) {
                        SimpleTextNode simpleTextNode = new SimpleTextNode(mainNavigationElement, hint);
                        hintsWrapper.add(simpleTextNode);
                    }
                    bestPracticeViolationNode.add(hintsWrapper);
                }
                if (bestPracticeViolation.getRelatedElements() != null && bestPracticeViolation.getRelatedElements().size() > 0) {
                    WarningNode errorsWrapper = new WarningNode(mainNavigationElement, "Related elements");
                    for (RelatedElementWrapper relatedElementWrapper : bestPracticeViolation.getRelatedElements()) {
                        WrapperNode errorElementWrapper = new WrapperNode(mainNavigationElement, relatedElementWrapper.getName());
                        for (Map.Entry<PsiElement, String> entry : relatedElementWrapper.getRelatedElementNameHashMap().entrySet()) {
                            errorElementWrapper.add(new ShowHideNode(entry.getKey().getNavigationElement(),entry.getKey(), entry.getKey().getTextRange(), this, entry.getValue() +" - highlight", entry.getValue() +" - delete highlight"));
                        }
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
