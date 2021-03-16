package com.testspector.view;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.view.report.GroupBy;
import com.testspector.view.report.TreeViewReport;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
    private JScrollBar scrollBar1;
    private JPanel panel1;
    private JPanel body;
    private JSplitPane splitPane;
    private JPanel processingWrapper;
    private JPanel contentWrapper;
    private JLabel clearConsole;
    private JPanel rightNav;
    private JPanel lefNavElementsWrapper;
    private JComboBox<GroupBy> groupByComboBox;
    private ConsoleView consoleView;
    private TreeViewReport treeViewReport;

    private TreeViewReport reportContent = null;

    public ToolWindowContent(Project project,RerunToolWindowContentAction rerunToolWindowContentAction) {
        this.project = project;
        consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        splitPane.setRightComponent(consoleView.getComponent());
        ((BasicSplitPaneDivider) splitPane.getComponent(0)).setBorder(BorderFactory.createEmptyBorder());
        leftNav.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));
        splitPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        rightNav.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));

        setupActionLabel(expand, EXPAND_ALL, false, () -> ProgressManager.getInstance().run(new Task.Backgroundable(project,"Expanding report nodes",true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
               reportContent.expandAll();
            }
        }));
        setupActionLabel(clearConsole, CLEAR, false, () -> this.consoleView.clear());
        setupActionLabel(collapse, COLLAPSE_ALL, false, () -> ProgressManager.getInstance().run(new Task.Backgroundable(project,"Expanding report nodes",true) {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                reportContent.collapseAll();
            }
        }));
        setupActionLabel(rerun, RERUN, false);
        Arrays.stream(lefNavElementsWrapper.getComponents()).filter(component -> component instanceof JLabel).forEach(leftNavComp -> {
            ((JLabel) leftNavComp).setBorder(new EmptyBorder(2, 0, 2, 0));
        });
        splitPane.setDividerLocation(panel1.getPreferredSize().width / 2);
        groupByComboBox.setEnabled(false);
        groupByComboBox.removeAllItems();
        Arrays.stream(GroupBy.values()).forEach(groupBy -> {
            groupByComboBox.addItem(groupBy);
        });
        groupByComboBox.addActionListener(e -> {
                    reportContent.groupBy((GroupBy) Objects.requireNonNull(groupByComboBox.getSelectedItem()));
                    panel1.revalidate();
                    panel1.repaint();
        });
        setOnRerun(rerunToolWindowContentAction);
    }

    private void setupActionLabel(JLabel label, CustomIcon customIcon, boolean enabled) {
        setupActionLabel(label, customIcon, enabled, () -> {
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


    public JSplitPane getSplitPane() {
        return splitPane;
    }

    public ConsoleView getConsoleView() {
        return consoleView;
    }


    public RerunToolWindowContentAction getRerunToolWindowContentAction() {
        return rerunToolWindowContentAction;
    }

    private void setOnRerun(RerunToolWindowContentAction rerunToolWindowContentAction){
        this.rerunToolWindowContentAction = rerunToolWindowContentAction;
        this.rerun.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                groupByComboBox.setEnabled(false);
                collapse.setEnabled(false);
                expand.setEnabled(false);
                contentWrapper.removeAll();
                contentWrapper.add(processingWrapper);
                rerun.setEnabled(false);
                panel1.revalidate();
                panel1.repaint();
                rerunToolWindowContentAction.rerun(ToolWindowContent.this);
            }
        });
    }

    public void cancel(){
        JLabel interruptedLabel = new JLabel("Cancelled");
        interruptedLabel.setIcon(WARNING.getBasic());
        groupByComboBox.setEnabled(false);
        collapse.setEnabled(false);
        expand.setEnabled(false);
        contentWrapper.removeAll();
        contentWrapper.add(interruptedLabel);
        panel1.revalidate();
        panel1.repaint();
        rerun.setEnabled(true);
    }

    public void addData(List<BestPracticeViolation> bestPracticeViolations) {
        getConsoleView().print(String.format("\n%d best practice violations found", bestPracticeViolations.size()), ConsoleViewContentType.SYSTEM_OUTPUT);
        this.contentWrapper.removeAll();
        if (bestPracticeViolations == null || bestPracticeViolations.size() == 0) {
            collapse.setEnabled(false);
            expand.setEnabled(false);
            JLabel noErrorsLabel = new JLabel("Not best practice violations found. Great job!");
            noErrorsLabel.setIcon(SUCCEEDED.getBasic());
            this.contentWrapper.add(noErrorsLabel);
        } else {
            collapse.setEnabled(true);
            expand.setEnabled(true);

            this.treeViewReport = new TreeViewReport(bestPracticeViolations, (GroupBy) groupByComboBox.getSelectedItem());
            this.reportContent = treeViewReport;
            this.contentWrapper.add(treeViewReport);
        }

        rerun.setEnabled(true);
        groupByComboBox.setEnabled(true);
        this.panel1.revalidate();
        this.panel1.repaint();
    }


}
