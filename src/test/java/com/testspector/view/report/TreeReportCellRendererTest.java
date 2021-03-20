package com.testspector.view.report;

import com.testspector.HeavyTestBase;
import com.testspector.model.enums.BestPractice;
import com.testspector.view.CustomIcon;
import org.easymock.EasyMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertAll;

public class TreeReportCellRendererTest extends HeavyTestBase {

    private TreeReportCellRenderer treeReportCellRenderer;

    @BeforeEach
    public void beforeEach() {
        this.treeReportCellRenderer = new TreeReportCellRenderer();
    }


    @Test
    public void getTreeCellRendererComponent_renderedObjectIsWrapperNode_ShouldReturnLabelWithIconPackageAndTextSameAsWrapperNodeName() {
        String wrapperNodeName = "Test";
        WrapperNode wrapperNode = new WrapperNode(null, wrapperNodeName);

        JLabel renderComponent = (JLabel) this.treeReportCellRenderer.getTreeCellRendererComponent(null, wrapperNode, false, false, false, 0, false);

        assertAll(
                () -> assertEquals("Different wrapper node name", wrapperNodeName, renderComponent.getText()),
                () -> assertSame("Different wrapper node icon", CustomIcon.PACKAGE.getBasic(), renderComponent.getIcon())
        );
    }

    @Test
    public void getTreeCellRendererComponent_renderedObjectIsWarningNode_ShouldReturnLabelWithIconWarningeAndTextSameAsWrapperNodeName() {
        String wrapperNodeName = "Test";
        String expectedLabelText = "<html><b>" + wrapperNodeName + "</b></html>";
        WarningNode warningNode = new WarningNode(null, wrapperNodeName);

        JLabel renderComponent = (JLabel) this.treeReportCellRenderer.getTreeCellRendererComponent(null, warningNode, false, false, false, 0, false);

        assertAll(
                () -> assertEquals("Different warning node name", expectedLabelText, renderComponent.getText()),
                () -> assertSame("Different warning node icon", CustomIcon.WARNING.getBasic(), renderComponent.getIcon())
        );
    }

    @Test
    public void getTreeCellRendererComponent_renderedObjectIsViolatedRuleNode_ShouldReturnPanelWithIconErrorAndFilledBrokenRule() {
        BestPractice violatedBestPractice = BestPractice.AT_LEAST_ONE_ASSERTION;
        String expectedLabelText = "Broken rule: " + violatedBestPractice.getDisplayName().toUpperCase();
        ViolatedRuleNode violatedRuleNode = new ViolatedRuleNode(null, violatedBestPractice);

        JPanel renderComponent = (JPanel) this.treeReportCellRenderer.getTreeCellRendererComponent(null, violatedRuleNode, false, false, false, 0, false);

        assertAll(
                () -> assertEquals("Different panel label text", expectedLabelText, ((JLabel) renderComponent.getComponent(0)).getText() + ((JLabel) renderComponent.getComponent(1)).getText()),
                () -> assertSame("Different warning node icon", CustomIcon.ERROR.getBasic(), ((JLabel) renderComponent.getComponent(0)).getIcon())
        );
    }

    @Test
    public void getTreeCellRendererComponent_renderedObjectIsLinkNode_ShouldReturnLabelWithLinkText() throws Exception {
        String expectedLinkText = "www.google.com";
        LinkNode linkNode = new LinkNode(new URI(expectedLinkText), expectedLinkText);

        JLabel renderComponent = (JLabel) this.treeReportCellRenderer.getTreeCellRendererComponent(null, linkNode, false, false, false, 0, false);

        assertEquals("Different link panel label text", expectedLinkText, renderComponent.getText());
    }

    @Test
    public void getTreeCellRendererComponent_renderedObjectIsInfoNode_ShouldReturnLabelWithIconInfoAndTextSameAsInfoNode() {
        String infoNodeName = "Test";
        String expectedText = "<html><b>" + infoNodeName + "</b></html>";
        InfoNode infoNode = new InfoNode(null, infoNodeName);

        JLabel renderComponent = (JLabel) this.treeReportCellRenderer.getTreeCellRendererComponent(null, infoNode, false, false, false, 0, false);

        assertAll(
                () -> assertEquals("Different info node name", expectedText, renderComponent.getText()),
                () -> assertSame("Different info node icon", CustomIcon.INFO.getBasic(), renderComponent.getIcon())
        );
    }

    @Test
    public void getTreeCellRendererComponent_renderedObjectIsShowHideNodeAndCodeIsNotHighlighted_ShouldReturnLabelWithIconShowAndTextSameAsOnShowLabelText() {
        String expectedText = "show";
        ShowHideNode showHideNode = new ShowHideNode(null, null, null, null, expectedText, null);

        JLabel renderComponent = (JLabel) this.treeReportCellRenderer.getTreeCellRendererComponent(null, showHideNode, false, false, false, 0, false);

        assertAll(
                () -> assertEquals("Different show hide node name", expectedText, renderComponent.getText()),
                () -> assertSame("Different show hide node icon", CustomIcon.SHOW.getBasic(), renderComponent.getIcon())
        );
    }

    @Test
    public void getTreeCellRendererComponent_renderedObjectIsShowHideNodeAndCodeIsHighlighted_ShouldReturnLabelWithIconHideAndTextSameAsOnHideLabelText() {
        String expectedText = "hide";
        ShowHideNode showHideNode = EasyMock.mock(ShowHideNode.class);
        EasyMock.expect(showHideNode.isCodeHighlighted()).andReturn(true).times(1);
        EasyMock.expect(showHideNode.getOnHideLabel()).andReturn(expectedText).times(1);
        EasyMock.replay(showHideNode);

        JLabel renderComponent = (JLabel) this.treeReportCellRenderer.getTreeCellRendererComponent(null, showHideNode, false, false, false, 0, false);

        assertAll(
                () -> assertEquals("Different show hide node name", expectedText, renderComponent.getText()),
                () -> assertSame("Different show hide node icon", CustomIcon.HIDE.getBasic(), renderComponent.getIcon())
        );
    }

    @Test
    public void getTreeCellRendererComponent_renderedObjectIsSimpleTextNode_ShouldReturnLabelWithTextSameAsSimpleTextNode() {
        String expectedText = "Some description";
        SimpleTextNode simpleTextNode = new SimpleTextNode(null, expectedText);

        JLabel renderComponent = (JLabel) this.treeReportCellRenderer.getTreeCellRendererComponent(null, simpleTextNode, false, false, false, 0, false);

        assertEquals("Different simple text node name", expectedText, renderComponent.getText());
    }

    @Test
    public void getTreeCellRendererComponent_renderedObjectIsDefaultMutableTreeNode_ShouldReturnEmptyLabel() {
       DefaultMutableTreeNode defaultMutableTreeNode = new DefaultMutableTreeNode();

        JLabel renderComponent = (JLabel) this.treeReportCellRenderer.getTreeCellRendererComponent(null, defaultMutableTreeNode, false, false, false, 0, false);

        assertAll(
                () -> assertEquals("No text should be present","", renderComponent.getText()),
                () -> assertNull("No icon should be present",  renderComponent.getIcon())
        );
    }
}
