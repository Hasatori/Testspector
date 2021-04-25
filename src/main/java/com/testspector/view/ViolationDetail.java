package com.testspector.view;

import javax.swing.*;

public class ViolationDetail {
    private JLabel problemHeadingLabel;
    private JTextPane problemDescription;
    private JLabel hintsHeadingLabel;
    private JPanel detailContent;
    private JTextPane hintsDescription;
    private JLabel testName;


    public ViolationDetail() {
        problemHeadingLabel.setIcon(CustomIcon.WARNING.getBasic());
        hintsHeadingLabel.setIcon(CustomIcon.INFO.getBasic());
    }

    public JTextPane getProblemDescription() {
        return problemDescription;
    }

    public JTextPane getHintsDescription() {
        return hintsDescription;
    }

    public JPanel getDetailContent() {
        return detailContent;
    }

    public JLabel getTestName() {
        return testName;
    }
}
