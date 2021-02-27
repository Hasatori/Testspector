package com.testspector.view;

import javax.swing.*;

import java.awt.*;
import java.awt.event.AWTEventListener;

import static com.intellij.openapi.util.IconLoader.findIcon;

public enum CustomIcon {

    LOGO(findIcon("/icons/logo.svg"), null, null, findIcon("/icons/logo.svg"), null, null),
    RERUN(findIcon("/icons/rerun_dark.svg"), findIcon("/icons/rerunHover_dark.svg"), findIcon("/icons/rerunDisabled_dark.svg"), findIcon("/icons/rerun_light.svg"), findIcon("/icons/rerunHover_light.svg"), findIcon("/icons/rerunDisabled_light.svg")),
    EXPAND_ALL(findIcon("/icons/expandAll_dark.svg"), findIcon("/icons/expandAllHover_dark.svg"), findIcon("/icons/expandAllDisabled_dark.svg"), findIcon("/icons/expandAll_light.svg"), findIcon("/icons/expandAllHover_light.svg"), findIcon("/icons/expandAllDisabled_light.svg")),
    COLLAPSE_ALL(findIcon("/icons/collapseAll_dark.svg"), findIcon("/icons/collapseAllHover_dark.svg"), findIcon("/icons/collapseAllDisabled_dark.svg"), findIcon("/icons/collapseAll_light.svg"), findIcon("/icons/collapseAllHover_light.svg"), findIcon("/icons/collapseAllDisabled_light.svg")),
    SHOW(findIcon("/icons/show_dark.svg"), findIcon("/icons/showHover_dark.svg"), findIcon("/icons/showDisabled_dark.svg"), findIcon("/icons/show_light.svg"), findIcon("/icons/showHover_light.svg"), findIcon("/icons/showDisabled_light.svg")),
    CLEAR(findIcon("/icons/delete_dark.svg"), findIcon("/icons/deleteHover_dark.svg"), null, findIcon("/icons/delete_light.svg"), findIcon("/icons/deleteHover_light.svg"), null),
    STOP(findIcon("/icons/stop_dark.svg"), findIcon("/icons/stopHover_dark.svg"), findIcon("/icons/stopDisabled_dark.svg"), findIcon("/icons/stop_light.svg"), findIcon("/icons/stopHover_light.svg"), findIcon("/icons/stopDisabled_light.svg")),
    SUCCEEDED(findIcon("/icons/succeeded_dark.svg"), null, null, findIcon("/icons/succeeded_light.svg"), null, null),
    WARNING(findIcon("/icons/balloonWarning_dark.svg"), null, null, findIcon("/icons/balloonWarning_light.svg"), null, null),
    INFO(findIcon("/icons/balloonInformation_dark.svg"), null, null, findIcon("/icons/balloonInformation_light.svg"), null, null),
    ERROR(findIcon("/icons/balloonError_dark.svg"), null, null, findIcon("/icons/balloonError_light.svg"), null, null),
    PACKAGE(findIcon("/icons/package_dark.svg"), null, null, findIcon("/icons/package_light.svg"), null, null),
    FILE(findIcon("/icons/file_dark.svg"), null, null, findIcon("/icons/file_light.svg"), null, null),
    HIDE(findIcon("/icons/hide_dark.svg"), null, null, findIcon("/icons/hide_light.svg"), null, null),
    DELETE(findIcon("/icons/exit_dark.svg"), null, null, findIcon("/icons/exit_light.svg"), null, null);


    private final Icon light;
    private final Icon lightHover;
    private final Icon lightDisabled;
    private final Icon dark;
    private final Icon darkHover;
    private final Icon darkDisabled;

    CustomIcon(Icon dark, Icon darkHover, Icon darkDisabled, Icon light, Icon lightHover, Icon lightDisabled) {
        this.light = light;
        this.lightHover = lightHover;
        this.lightDisabled = lightDisabled;
        this.dark = dark;
        this.darkHover = darkHover;
        this.darkDisabled = darkDisabled;
    }


    public Icon getBasic() {
        boolean isDarcula = com.intellij.util.ui.UIUtil.isUnderDarcula();
        return isDarcula ? dark:light;
    }

    public Icon getHover() {
        boolean isDarcula = com.intellij.util.ui.UIUtil.isUnderDarcula();
        return isDarcula ? darkHover:lightHover;
    }

    public Icon getDisabled() {
        boolean isDarcula = com.intellij.util.ui.UIUtil.isUnderDarcula();
        return isDarcula ? darkDisabled:lightDisabled;
    }


}
