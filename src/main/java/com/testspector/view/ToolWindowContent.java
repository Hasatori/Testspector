package com.testspector.view;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.view.report.TreeViewReport;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;

import static com.testspector.view.CustomIcon.*;

public class ToolWindowContent {

    private static final Cursor CURSOR_HAND = new Cursor(Cursor.HAND_CURSOR);
    private static final Cursor CURSOR_DEFAULT = new Cursor(Cursor.DEFAULT_CURSOR);
    private static final Color BORDER_COLOR = new Color(50, 50, 50);
    private final Project project;
    private RerunToolWindowContentAction rerunToolWindowContentAction;


    private JPanel leftNav;
    private JLabel rerun;
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
    private JPanel rightNav;
    private JLabel stop;
    private JPanel lefNavElementsWrapper;
    private ConsoleView consoleView;

    private TreeViewReport reportContent = null;

    public ToolWindowContent(Project project) {
        this.project = project;
        consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        splitPane.setRightComponent(consoleView.getComponent());
        ((BasicSplitPaneDivider) splitPane.getComponent(0)).setBorder(BorderFactory.createEmptyBorder());
        leftNav.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));
        splitPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        rightNav.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        setupActionLabel(expand, EXPAND_ALL,  false, () -> this.reportContent.expandAll());
        setupActionLabel(clearConsole,CLEAR, false, () -> this.consoleView.clear());
        setupActionLabel(collapse, COLLAPSE_ALL, false, () -> this.reportContent.collapseAll());
        setupActionLabel(highlightAll, SHOW, false, () -> {
            this.reportContent.highlightAll();
            this.contentWrapper.repaint();
        });
        setupActionLabel(stop, STOP,  false);
        setupActionLabel(rerun, RERUN,  false);
        Arrays.stream(lefNavElementsWrapper.getComponents()).filter(component -> component instanceof JLabel).forEach(leftNavComp -> {
            ((JLabel) leftNavComp).setBorder(new EmptyBorder(2, 0, 2, 0));
        });
        splitPane.setDividerLocation(panel1.getPreferredSize().width / 2);
    }

    private void setupActionLabel(JLabel label, CustomIcon customIcon, boolean enabled) {
        setupActionLabel(label,customIcon, enabled, () -> {
        });
    }

    private void setupActionLabel(JLabel label, CustomIcon customIcon, boolean enabled, Runnable onClick) {
        label.setIcon(customIcon.getBasic());
        label.setDisabledIcon(customIcon.getDisabled());
        label.setEnabled(enabled);
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                label.setCursor(CURSOR_HAND);
                label.setIcon(customIcon.getHover());
            }
        });

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                label.setIcon(customIcon.getBasic());
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

    public RerunToolWindowContentAction getRerunToolWindowContentAction() {
        return rerunToolWindowContentAction;
    }

    public void start(RerunToolWindowContentAction rerunToolWindowContentAction, Runnable onStop) {

        this.contentWrapper.removeAll();
        this.contentWrapper.add(processingWrapper);
        this.panel1.revalidate();
        this.panel1.repaint();
        rerun.setEnabled(false);
        this.rerunToolWindowContentAction = rerunToolWindowContentAction;
        this.rerun.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                collapse.setEnabled(false);
                expand.setEnabled(false);
                highlightAll.setEnabled(false);
                contentWrapper.removeAll();
                contentWrapper.add(processingWrapper);
                stop.setEnabled(true);
                rerun.setEnabled(false);
                panel1.revalidate();
                panel1.repaint();
                rerunToolWindowContentAction.rerun(ToolWindowContent.this);
            }
        });

        this.stop.setEnabled(true);
        this.stop.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                stop.setEnabled(false);
                rerun.setEnabled(true);
                collapse.setEnabled(false);
                expand.setEnabled(false);
                highlightAll.setEnabled(false);
                contentWrapper.removeAll();
                JLabel interruptedLabel = new JLabel("Interrupted");
                interruptedLabel.setIcon(WARNING.getBasic());
                contentWrapper.add(interruptedLabel);
                panel1.revalidate();
                panel1.repaint();
                onStop.run();
            }
        });
    }

    public void showReport(List<BestPracticeViolation> bestPracticeViolations) {
        this.contentWrapper.removeAll();
        if (bestPracticeViolations == null || bestPracticeViolations.size() == 0) {
            collapse.setEnabled(false);
            expand.setEnabled(false);
            highlightAll.setEnabled(false);
            JLabel noErrorsLabel = new JLabel("Not best practice violations found. Great job!");
            noErrorsLabel.setIcon(SUCCEEDED.getBasic());
            this.contentWrapper.add(noErrorsLabel);
        } else {
            collapse.setEnabled(true);
            expand.setEnabled(true);
            highlightAll.setEnabled(true);
            TreeViewReport treeViewReport = new TreeViewReport(bestPracticeViolations, this.project);
            this.reportContent = treeViewReport;
            this.contentWrapper.add(treeViewReport);
            treeViewReport.expandAll();
        }
        stop.setEnabled(false);
        rerun.setEnabled(true);
        this.panel1.revalidate();
        this.panel1.repaint();
    }


}
