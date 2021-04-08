package com.testspector.view.report;

import com.intellij.ui.JBColor;
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
        if (value instanceof WrapperNode) {
            return getWrapperNodeComponent((WrapperNode) value);
        } else if (value instanceof WarningNode) {
            return getWarningNodeComponent((WarningNode) value);
        } else if (value instanceof ViolatedRuleNode) {
            return getViolatedRuleComponent((ViolatedRuleNode) value);
        } else if (value instanceof LinkNode) {
            return getLinkNodeComponent((LinkNode) value);
        } else if (value instanceof InfoNode) {
            return getInfoNodeComponent((InfoNode) value);
        } else if (value instanceof ShowHideNode) {
            return getShowHideNodeComponent((ShowHideNode) value);
        } else if (value instanceof SimpleTextNode) {
            return getSimpleTextNodeComponent(value);
        }
        return null;
    }

    private JLabel getWrapperNodeComponent(WrapperNode wrapperNode) {
        JLabel label = new JLabel();
        label.setText(wrapperNode.getName());
        label.setIcon(CustomIcon.PACKAGE.getBasic());
        return label;
    }

    private JLabel getWarningNodeComponent(WarningNode warningNode) {
        JLabel label = new JLabel();
        label.setText(String.format("<html><b>%s</b></html>", warningNode.getDescription()));
        label.setIcon(CustomIcon.WARNING.getBasic());
        return label;
    }

    private JPanel getViolatedRuleComponent(ViolatedRuleNode violatedRuleNode) {
        JLabel label = new JLabel();
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
        return panel;
    }

    private JLabel getLinkNodeComponent(LinkNode linkNode) {
        JLabel label = new JLabel();
        label.setText(linkNode.getLinkText());
        label.setForeground(JBColor.CYAN);
        Font font = label.getFont();
        Map<TextAttribute, Object> attributes = new HashMap<>(font.getAttributes());
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        label.setFont(font.deriveFont(attributes));
        return label;
    }

    private JLabel getInfoNodeComponent(InfoNode simpleTextNode) {
        JLabel label = new JLabel();
        label.setText(String.format("<html><b>%s</b></html>", simpleTextNode.getDescription()));
        label.setIcon(CustomIcon.INFO.getBasic());
        return label;
    }

    private JLabel getShowHideNodeComponent(ShowHideNode showHideNode) {
        JLabel label = new JLabel();
        if (showHideNode.isCodeHighlighted()) {
            label.setText(showHideNode.getOnHideLabel());
            label.setIcon(CustomIcon.HIDE.getBasic());
        } else {
            label.setText(showHideNode.getOnShowLabel());
            label.setIcon(CustomIcon.SHOW.getBasic());
        }
        return label;
    }

    private JLabel getSimpleTextNodeComponent(Object value) {
        JLabel label = new JLabel();
        label.setText(((SimpleTextNode) value).getDescription());
        return label;
    }
}
