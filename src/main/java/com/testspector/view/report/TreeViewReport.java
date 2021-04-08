package com.testspector.view.report;

import com.intellij.psi.PsiElement;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.model.checking.RelatedElementWrapper;
import com.testspector.model.enums.BestPractice;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeViewReport extends JTree {

    private static final String HIGHLIGHT_LABEL_TEXT = "highlight";
    private static final String DELETE_HIGHLIGHTING_LABEL_TEXT = "delete highlighting";

    private final List<BestPracticeViolation> bestPracticeViolations;
    private final DefaultTreeModel byTestsModel;
    private final DefaultTreeModel byViolatedBestPracticesModel;

    public TreeViewReport(List<BestPracticeViolation> bestPracticeViolations, GroupBy groupBy) {
        super();
        this.bestPracticeViolations = bestPracticeViolations;
        this.byTestsModel = this.groupByTests();
        this.byViolatedBestPracticesModel = this.groupByBestPractice();
        this.groupBy(groupBy);
        this.setAlignmentX(LEFT_ALIGNMENT);
        this.setAlignmentY(TOP_ALIGNMENT);
        this.setRootVisible(false);
        this.setCellRenderer(new TreeReportCellRenderer());
        this.addMouseListener(new TreeReportMouseListener());
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
            }
        }
    }

    public void groupBy(GroupBy groupBy) {
        switch (groupBy) {
            case TESTS:
                this.setModel(this.byTestsModel);
                break;
            case VIOLATED_BEST_PRACTICE:
                this.setModel(this.byViolatedBestPracticesModel);
                break;
        }
    }

    private DefaultTreeModel groupByBestPractice() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        HashMap<BestPractice, List<BestPracticeViolation>> psiElementBestPracticeViolationHashMap = getBestPracticeViolationsMap();
        psiElementBestPracticeViolationHashMap.forEach((bestPractice, group) -> {
            ViolatedRuleNode groupNode = new ViolatedRuleNode(bestPractice);
            groupNode.add(createLinkNode(bestPractice));
            for (BestPracticeViolation bestPracticeViolation : group) {
                PsiElement mainNavigationElement = bestPracticeViolation.getTestMethodElement().getNavigationElement();
                WrapperNode bestPracticeViolationNode = new WrapperNode(mainNavigationElement, bestPracticeViolation.getName());
                bestPracticeViolationNode.add(createShowHideNode(mainNavigationElement, bestPracticeViolation));
                bestPracticeViolationNode.add(new WarningNode(mainNavigationElement, bestPracticeViolation.getProblemDescription()));
                if (bestPracticeViolation.getHints() != null && bestPracticeViolation.getHints().size() > 0) {
                    bestPracticeViolationNode.add(createInfoNode(mainNavigationElement, bestPracticeViolation));
                }
                if (bestPracticeViolation.getRelatedElements() != null && bestPracticeViolation.getRelatedElements().size() > 0) {
                    bestPracticeViolationNode.add(createWarningNode(mainNavigationElement, bestPracticeViolation));
                }
                groupNode.add(bestPracticeViolationNode);
            }

            root.add(groupNode);
        });
        return new DefaultTreeModel(root);
    }

    private HashMap<BestPractice, List<BestPracticeViolation>> getBestPracticeViolationsMap() {
        HashMap<BestPractice, List<BestPracticeViolation>> psiElementBestPracticeViolationHashMap = new HashMap<>();
        for (BestPracticeViolation bestPracticeViolation : bestPracticeViolations) {
            List<BestPracticeViolation> foundGroup = psiElementBestPracticeViolationHashMap.get(bestPracticeViolation.getViolatedRule());
            if (foundGroup == null) {
                foundGroup = new ArrayList<>();
            }
            foundGroup.add(bestPracticeViolation);
            psiElementBestPracticeViolationHashMap.put(bestPracticeViolation.getViolatedRule(), foundGroup);
        }
        return psiElementBestPracticeViolationHashMap;
    }


    private DefaultTreeModel groupByTests() {
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
            WrapperNode groupNode = new WrapperNode(mainNavigationElement, group.get(0).getName());
            for (BestPracticeViolation bestPracticeViolation : group) {
                ViolatedRuleNode bestPracticeViolationNode = new ViolatedRuleNode(mainNavigationElement, bestPracticeViolation.getViolatedRule());
                bestPracticeViolationNode.add(createLinkNode(bestPracticeViolation.getViolatedRule()));
                bestPracticeViolationNode.add(createShowHideNode(mainNavigationElement, bestPracticeViolation));
                bestPracticeViolationNode.add(new WarningNode(mainNavigationElement, bestPracticeViolation.getProblemDescription()));
                if (bestPracticeViolation.getHints() != null && bestPracticeViolation.getHints().size() > 0) {
                    bestPracticeViolationNode.add(createInfoNode(mainNavigationElement, bestPracticeViolation));
                }
                if (bestPracticeViolation.getRelatedElements() != null && bestPracticeViolation.getRelatedElements().size() > 0) {
                    bestPracticeViolationNode.add(createWarningNode(mainNavigationElement, bestPracticeViolation));
                }
                groupNode.add(bestPracticeViolationNode);
            }
            root.add(groupNode);
        });
        return new DefaultTreeModel(root);
    }

    private ShowHideNode createShowHideNode(PsiElement mainNavigationElement, BestPracticeViolation bestPracticeViolation) {
        return new ShowHideNode(
                mainNavigationElement,
                bestPracticeViolation.getTestMethodElement(),
                bestPracticeViolation.getTestMethodTextRange(),
                this,
                HIGHLIGHT_LABEL_TEXT,
                DELETE_HIGHLIGHTING_LABEL_TEXT);
    }

    private InfoNode createInfoNode(PsiElement mainNavigationElement, BestPracticeViolation bestPracticeViolation) {
        InfoNode hintsWrapper = new InfoNode(mainNavigationElement, "Hints");
        for (String hint : bestPracticeViolation.getHints()) {
            SimpleTextNode simpleTextNode = new SimpleTextNode(mainNavigationElement, hint);
            hintsWrapper.add(simpleTextNode);
        }
        return hintsWrapper;
    }

    private WarningNode createWarningNode(PsiElement mainNavigationElement, BestPracticeViolation bestPracticeViolation) {
        WarningNode warningNode = new WarningNode(mainNavigationElement, "Related elements");
        for (RelatedElementWrapper relatedElementWrapper : bestPracticeViolation.getRelatedElements()) {
            WrapperNode errorElementWrapper = new WrapperNode(mainNavigationElement, relatedElementWrapper.getName());
            for (Map.Entry<PsiElement, String> entry : relatedElementWrapper.getRelatedElementNameHashMap().entrySet()) {
                errorElementWrapper.add(new ShowHideNode(
                        entry.getKey().getNavigationElement(),
                        entry.getKey(),
                        entry.getKey().getTextRange(),
                        this,
                        entry.getValue() + " - " + HIGHLIGHT_LABEL_TEXT,
                        entry.getValue() + " - " + DELETE_HIGHLIGHTING_LABEL_TEXT));
            }
            warningNode.add(errorElementWrapper);
        }
        return warningNode;
    }

    private LinkNode createLinkNode(BestPractice bestPractice) {
        return new LinkNode(bestPractice.getWebPageHyperlink(), "get more information about the rule");
    }


}
