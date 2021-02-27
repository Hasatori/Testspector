package com.testspector.view.report;

import com.testspector.view.CustomIcon;

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
        if (value instanceof WrapperNode) {
            WrapperNode wrapperNode = (WrapperNode) value;
            label.setText(wrapperNode.getName());
            label.setIcon(CustomIcon.PACKAGE.getBasic());
            return label;
        } else if (value instanceof WarningNode) {
            WarningNode warningNode = (WarningNode) value;
            label.setText(String.format("<html><b>%s</b></html>", warningNode.getDescription()));
            label.setIcon(CustomIcon.WARNING.getBasic());
            return label;
        } else if (value instanceof ViolatedRuleNode) {
            ViolatedRuleNode violatedRuleNode = (ViolatedRuleNode) value;
            label.setText("Broken rule: ");
            label.setIcon(CustomIcon.ERROR.getBasic());
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
            panel.add(label);
            JLabel ruleLabel = new JLabel(violatedRuleNode.getViolatedBestPractice().getDisplayName().toUpperCase());
            Font ruleFont = ruleLabel.getFont();
            Map<TextAttribute, Object> ruleAttributes = new HashMap<>(ruleFont.getAttributes());
            ruleAttributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
            ruleLabel.setFont(ruleFont.deriveFont(ruleAttributes));
            panel.add(ruleLabel);
            panel.add(new JLabel(" - "));
            JLabel linkLabel = new JLabel();
            linkLabel.setText("get more information");
            linkLabel.setForeground(Color.CYAN);
            Font font = linkLabel.getFont();
            Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
            attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            linkLabel.setFont(font.deriveFont(attributes));
            panel.add(linkLabel);
            return panel;
        } else if (value instanceof InfoNode) {
            InfoNode simpleTextNode = (InfoNode) value;
            label.setText(String.format("<html><b>%s</b></html>", simpleTextNode.getDescription()));
            label.setIcon(CustomIcon.INFO.getBasic());
            return label;
        } else if (value instanceof ShowHideNode) {
            ShowHideNode showHideNode = (ShowHideNode) value;
            if (showHideNode.isCodeHighlighted()) {
                label.setText(showHideNode.getOnHideLabel());
                label.setIcon(CustomIcon.HIDE.getBasic());
            } else {
                label.setText(showHideNode.getOnShowLabel());
                label.setIcon(CustomIcon.SHOW.getBasic());
            }
        } else if (value instanceof SimpleTextNode) {
            label.setText(((SimpleTextNode) value).getDescription());

        }
        return label;
    }
}
