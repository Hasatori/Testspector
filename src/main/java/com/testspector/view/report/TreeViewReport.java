package com.testspector.view.report;

import com.intellij.openapi.project.Project;
import com.testspector.model.checking.BestPracticeViolation;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.List;

public class TreeViewReport extends JTree {

    private final Project project;

    public TreeViewReport(List<BestPracticeViolation> bestPracticeViolations, Project project) {
        super();
        this.project = project;
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        for (BestPracticeViolation bestPracticeViolation : bestPracticeViolations) {
            DefaultMutableTreeNode bestPracticeViolationNode = new BestPracticeViolationWrapperNode(bestPracticeViolation);
            bestPracticeViolationNode.add(new ViolatedRuleNode(bestPracticeViolation));
            bestPracticeViolationNode.add(new ShowHideNode(bestPracticeViolation, project, this));
            bestPracticeViolationNode.add(new DescriptionNode(bestPracticeViolation));
            root.add(bestPracticeViolationNode);
        }
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
        for (int i = 0; i < root.getChildCount(); i++) {
            TreeNode node = root.getChildAt(i);
            if (node instanceof BestPracticeViolationNode) {
                BestPracticeViolationNode bestPracticeViolationNode = (BestPracticeViolationNode) node;
                this.expandPath(new TreePath(bestPracticeViolationNode.getPath()));
            }
        }
    }

    public void collapseAll() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) this.getModel().getRoot();
        for (int i = 0; i < root.getChildCount(); i++) {
            TreeNode node = root.getChildAt(i);
            if (node instanceof BestPracticeViolationNode) {
                BestPracticeViolationNode bestPracticeViolationNode = (BestPracticeViolationNode) node;
                this.collapsePath(new TreePath(bestPracticeViolationNode.getPath()));
            }
        }
    }


}
