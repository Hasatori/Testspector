package com.testspector.gui.report;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

public class TreeReportCellRenderer implements TreeCellRenderer {
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JLabel label = new JLabel();
        if (value instanceof BestPracticeViolationWrapperNode) {
            BestPracticeViolationWrapperNode bestPracticeViolationWrapperNode = (BestPracticeViolationWrapperNode) value;
            label.setText(bestPracticeViolationWrapperNode.getName());
            label.setIcon(IconLoader.getIcon("/icons/package.svg"));
            return label;
        } else if (value instanceof DescriptionNode) {
            DescriptionNode descriptionNode = (DescriptionNode) value;
            label.setText(String.format("<html><b>%s</b></html>", descriptionNode.getBestPracticeViolation().getDescription()));
            label.setIcon(IconLoader.getIcon("/icons/balloonWarning_dark.svg"));
            return label;
        } else if (value instanceof ViolatedRuleNode) {
            ViolatedRuleNode violatedRuleNode = (ViolatedRuleNode) value;
            label.setText("Broken rule: ");
            label.setIcon(IconLoader.getIcon("/icons/file.svg"));
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
            panel.add(label);
            JLabel ruleLabel = new JLabel(violatedRuleNode.getBestPracticeViolation().getViolatedRule().getDisplayName());
            ruleLabel.setForeground(Color.RED);
            Font ruleFont = ruleLabel.getFont();
            Map<TextAttribute, Object> ruleAttributes = new HashMap<>(ruleFont.getAttributes());
            ruleAttributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
            ruleLabel.setFont(ruleFont.deriveFont(ruleAttributes));
            panel.add(ruleLabel);
            JLabel linkLabel = new JLabel();
            linkLabel.setText(" - get more information");
            linkLabel.setForeground(Color.CYAN);
            Font font = linkLabel.getFont();
            Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
            attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            linkLabel.setFont(font.deriveFont(attributes));
            panel.add(linkLabel);
            return panel;
        } else if (value instanceof ShowHideNode) {
            ShowHideNode showHideNode = (ShowHideNode) value;
            if (showHideNode.isCodeHighlighted()) {
                label.setText("Delete highlighting of the code");
                label.setIcon(IconLoader.getIcon("/icons/hide_dark.svg"));
            } else {
                label.setText("Highlight problematic code");
                label.setIcon(IconLoader.getIcon("/icons/show_dark.svg"));
            }

        }
        return label;
    }
}
