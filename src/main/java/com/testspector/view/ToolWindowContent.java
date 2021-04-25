package com.testspector.view;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.testspector.model.checking.BestPracticeViolation;
import com.testspector.view.report.GroupBy;
import com.testspector.view.report.TreeReportMouseListener;
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
import java.util.stream.Collectors;

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
    private JPanel lefNavElementsWrapper;
    private JComboBox<GroupBy> groupByComboBox;
    private JLabel processingLabel;
    private JLabel openToDisplayDetailsLabel;
    private JPanel detailWrapper;
    private TreeViewReport treeViewReport;

    private TreeViewReport reportContent = null;

    public ToolWindowContent(Project project, RerunToolWindowContentAction rerunToolWindowContentAction) {
        this.openToDisplayDetailsLabel.setIcon(INFO.getBasic());
        this.project = project;
        ((BasicSplitPaneDivider) splitPane.getComponent(0)).setBorder(BorderFactory.createEmptyBorder());
        leftNav.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));
        splitPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR));
        Arrays.stream(lefNavElementsWrapper.getComponents()).filter(component -> component instanceof JLabel).forEach(leftNavComp -> {
            ((JLabel) leftNavComp).setBorder(new EmptyBorder(2, 0, 2, 0));
        });
        splitPane.setDividerLocation(panel1.getPreferredSize().width / 2);
        setupActions();
        setOnRerun(rerunToolWindowContentAction);
    }

    private void setupActions() {
        setupActionLabel(expand, EXPAND_ALL, false, () -> ProgressManager.getInstance().run(createSimpleCancellableTestTask("Expanding Report Nodes", this::expandAll)));
        setupActionLabel(collapse, COLLAPSE_ALL, false, () -> ProgressManager.getInstance().run(createSimpleCancellableTestTask("Collapsing Report Nodes", this::collapseAll)));
        setupActionLabel(rerun, RERUN, false);
        groupByComboBox.setEnabled(false);
        groupByComboBox.removeAllItems();
        Arrays.stream(GroupBy.values()).forEach(groupBy -> {
            groupByComboBox.addItem(groupBy);
        });
        groupByComboBox.setSelectedItem(GroupBy.VIOLATED_BEST_PRACTICE);
        groupByComboBox.addActionListener(e -> {
            ProgressManager.getInstance().run(new Task.Modal(project, "Grouping Violations by " + ((GroupBy) groupByComboBox.getSelectedItem()).getDisplayName(), false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    indicator.setIndeterminate(true);
                    groupBy();
                }
            });
        });
    }

    private Task.Backgroundable createSimpleCancellableTestTask(String title, Runnable onRun) {
        return new Task.Backgroundable(project, title, true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                onRun.run();
            }
        };
    }

    private void collapseAll() {
        groupByComboBox.setEnabled(false);
        collapse.setEnabled(false);
        expand.setEnabled(false);
        reportContent.collapseAll();
        collapse.setEnabled(true);
        expand.setEnabled(true);
        groupByComboBox.setEnabled(true);
    }

    private void expandAll() {
        groupByComboBox.setEnabled(false);
        collapse.setEnabled(false);
        expand.setEnabled(false);
        reportContent.expandAll();
        collapse.setEnabled(true);
        expand.setEnabled(true);
        groupByComboBox.setEnabled(true);
    }

    private void groupBy() {
        ApplicationManager.getApplication().invokeLater(() -> {
            groupByComboBox.setEnabled(false);
            rerun.setEnabled(false);
            collapse.setEnabled(false);
            expand.setEnabled(false);
            rerun.setEnabled(false);
            processingLabel.setText("Grouping Violations by " + ((GroupBy) groupByComboBox.getSelectedItem()).getDisplayName());
            panel1.revalidate();
            panel1.repaint();
        });
        ApplicationManager.getApplication().runReadAction(() -> {
            reportContent.groupBy((GroupBy) Objects.requireNonNull(groupByComboBox.getSelectedItem()));
        });
        ApplicationManager.getApplication().invokeLater(() -> {
            groupByComboBox.setEnabled(true);
            rerun.setEnabled(true);
            collapse.setEnabled(true);
            expand.setEnabled(true);
            panel1.revalidate();
            panel1.repaint();
        });
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


    public RerunToolWindowContentAction getRerunToolWindowContentAction() {
        return rerunToolWindowContentAction;
    }

    private void setOnRerun(RerunToolWindowContentAction rerunToolWindowContentAction) {
        this.rerunToolWindowContentAction = rerunToolWindowContentAction;
        this.rerun.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                groupByComboBox.setEnabled(false);
                collapse.setEnabled(false);
                expand.setEnabled(false);
                contentWrapper.removeAll();
                processingLabel.setText("Processing");
                contentWrapper.add(processingWrapper);
                rerun.setEnabled(false);
                panel1.revalidate();
                panel1.repaint();
                rerunToolWindowContentAction.rerun(ToolWindowContent.this);
            }
        });
    }

    public void cancel() {
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

    public void setData(List<BestPracticeViolation> bestPracticeViolations) {
        this.contentWrapper.removeAll();
        if (bestPracticeViolations.size() == 0) {
            collapse.setEnabled(false);
            expand.setEnabled(false);
            JLabel noErrorsLabel = new JLabel("Not best practice violations found. Great job!");
            noErrorsLabel.setIcon(SUCCEEDED.getBasic());
            this.contentWrapper.add(noErrorsLabel);
        } else {
            collapse.setEnabled(true);
            expand.setEnabled(true);

            this.treeViewReport = new TreeViewReport(new TreeReportMouseListener(this), bestPracticeViolations, (GroupBy) groupByComboBox.getSelectedItem());
            this.reportContent = treeViewReport;
            this.contentWrapper.add(treeViewReport);
        }

        rerun.setEnabled(true);
        groupByComboBox.setEnabled(true);
        this.panel1.revalidate();
        this.panel1.repaint();
    }

    public void violationOpened(BestPracticeViolation bestPracticeViolation) {
        this.detailWrapper.removeAll();
        ViolationDetail violationDetail = new ViolationDetail();
        violationDetail.getProblemDescription().setText(bestPracticeViolation.getProblemDescription());
        if (violationDetail.getHintsDescription() != null) {
            violationDetail.getHintsDescription().setText(bestPracticeViolation.getHints().stream().map(hint -> "-    " + hint + "\n").collect(Collectors.joining("\n")));
        }
        violationDetail.getTestName().setText(bestPracticeViolation.getName());
        this.detailWrapper.add(violationDetail.getDetailContent());
        this.detailWrapper.revalidate();
        this.detailWrapper.repaint();
    }

    public void leaveViolationDetail() {
        this.detailWrapper.removeAll();
        this.detailWrapper.add(openToDisplayDetailsLabel);
        this.detailWrapper.revalidate();
        this.detailWrapper.repaint();
    }

}
