package com.testspector.gui;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.testspector.checking.BestPracticeViolation;
import com.testspector.gui.report.TreeViewReport;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ToolWindowContent {


    private static final Icon RERUN_ICON = IconLoader.getIcon("/icons/rerun.svg");
    private static final Icon RERUN_ICON_HOVER = IconLoader.getIcon("/icons/rerunHover.svg");
    private static final Icon EXPAND_ALL_ICON = IconLoader.getIcon("/icons/expandAll.svg");
    private static final Icon EXPAND_ALL_ICON_HOVER = IconLoader.getIcon("/icons/expandAllHover.svg");
    private static final Icon COLLAPSE_ALL_ICON_HOVER = IconLoader.getIcon("/icons/collapseAllHover.svg");
    private static final Icon COLLAPSE_ALL_ICON = IconLoader.getIcon("/icons/collapseAll.svg");
    private static final Icon HIGHLIGHT_ALL_ICON = IconLoader.getIcon("/icons/show_dark.svg");
    private static final Icon HIGHLIGHT_ALL_ICON_HOVER = IconLoader.getIcon("/icons/show_darkHover.svg");
    private static final Icon HIDE_ALL_ICON = IconLoader.getIcon("/icons/hide_dark.svg");
    private static final Icon HIDE_ALL_ICON_HOVER = IconLoader.getIcon("/icons/hide_darkHover.svg");
    private static final Icon CLEAR_CONSOLE_ICON = IconLoader.getIcon("/icons/delete_dark.svg");
    private static final Icon CLEAR_CONSOLE_ICON_HOVER = IconLoader.getIcon("/icons/delete_darkHover.svg");
    private static final Cursor CURSOR_HAND = new Cursor(Cursor.HAND_CURSOR);
    private static final Cursor CURSOR_DEFAULT = new Cursor(Cursor.DEFAULT_CURSOR);
    private static final Color BORDER_COLOR = new Color(50, 50, 50);
    private final Project project;


    private JPanel leftNav;
    private JLabel rerun;
    private JPanel topNav;
    private JComboBox comboBox1;
    private JComboBox comboBox2;
    private JLabel expand;
    private JLabel collapse;
    private JTree reportTree;
    private JScrollBar scrollBar1;

    private JPanel panel1;
    private JPanel body;
    private JSplitPane splitPane;
    private JLabel highlightAll;
    private JPanel processingWrapper;
    private JPanel contentWrapper;
    private JLabel clearConsole;
    private ConsoleView consoleView;

    private TreeViewReport reportContent = null;


    public ToolWindowContent(Project project, RerunToolWindowContentAction rerunToolWindowContentAction) {
        this.project = project;
        consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        splitPane.setRightComponent(consoleView.getComponent());
        splitPane.getComponent(0).setForeground(Color.cyan);
        splitPane.getComponent(0).setBackground(Color.cyan);
        ((BasicSplitPaneDivider) splitPane.getComponent(0)).setBorder(BorderFactory.createEmptyBorder());
        leftNav.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));
        splitPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        setupActionLabel(expand, EXPAND_ALL_ICON, EXPAND_ALL_ICON_HOVER, () -> this.reportContent.expandAll());
        setupActionLabel(clearConsole, CLEAR_CONSOLE_ICON, CLEAR_CONSOLE_ICON_HOVER, () -> this.consoleView.clear());
        setupActionLabel(collapse, COLLAPSE_ALL_ICON, COLLAPSE_ALL_ICON_HOVER, () -> this.reportContent.collapseAll());
        setupActionLabel(rerun, RERUN_ICON, RERUN_ICON_HOVER, () -> {
            this.contentWrapper.remove(reportContent);
            this.contentWrapper.add(processingWrapper);
            this.panel1.validate();
            this.panel1.repaint();
            rerunToolWindowContentAction.rerun(this);
        });
        setupActionLabel(highlightAll, HIGHLIGHT_ALL_ICON, HIGHLIGHT_ALL_ICON_HOVER, () -> {
                    this.reportContent.highlightAll();
                    this.contentWrapper.repaint();
                }
        );
        splitPane.setDividerLocation(panel1.getPreferredSize().width / 2);
    }

    private void setupActionLabel(JLabel label, Icon icon, Icon hoverIcon, Runnable onClick) {
        label.setIcon(icon);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                label.setCursor(CURSOR_HAND);
                label.setIcon(hoverIcon);
            }
        });

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                label.setIcon(icon);
                label.setCursor(CURSOR_DEFAULT);
            }
        });
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                onClick.run();
            }
        });
    }

    public JPanel getPanel1() {
        return panel1;
    }

    public JTree getReportTree() {
        return reportTree;
    }

    public JSplitPane getSplitPane() {
        return splitPane;
    }

    public ConsoleView getConsoleView() {
        return consoleView;
    }

    public JLabel getHighlightAll() {
        return highlightAll;
    }


    public void showReport(List<BestPracticeViolation> bestPracticeViolations) {
        this.contentWrapper.removeAll();
        TreeViewReport treeViewReport = new TreeViewReport(bestPracticeViolations, this.project);
        this.reportContent = treeViewReport;
        this.contentWrapper.add(treeViewReport);
        treeViewReport.expandAll();
        this.panel1.revalidate();
        this.panel1.repaint();
    }


}
