package io.leego.sql.parser;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.JBMenuItem;
import com.intellij.openapi.ui.JBPopupMenu;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * @author Yihleego
 */
public class ConsoleToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JBColor bg = new JBColor(new Color(60, 63, 65), new Color(60, 63, 65));

        JBTextArea textArea = new JBTextArea();
        textArea.setLineWrap(true);
        textArea.setAutoscrolls(true);
        textArea.setBackground(bg);
        textArea.setFont(Font.getFont("Source Code Pro"));
        textArea.setMargin(JBUI.insets(0, 2, 2, 0));

        JBMenuItem scrollMenuItem = new JBMenuItem("Scroll to End");
        JBMenuItem clearMenuItem = new JBMenuItem("Clear All");

        JBPopupMenu popupMenu = new JBPopupMenu();
        popupMenu.add(scrollMenuItem);
        popupMenu.add(clearMenuItem);
        textArea.setComponentPopupMenu(popupMenu);

        /*JBSlidingPanel slidingPanel = new JBSlidingPanel();
        JBCheckBoxMenuItem warpLineCheckBox = new JBCheckBoxMenuItem();
        JBCheckBoxMenuItem scrollEndCheckBox = new JBCheckBoxMenuItem();
        warpLineCheckBox.setText("warp");
        scrollEndCheckBox.setText("end");
        warpLineCheckBox.setFocusPainted(true);
        scrollEndCheckBox.setFocusPainted(true);
        warpLineCheckBox.addChangeListener(e -> textArea.setLineWrap(!textArea.getLineWrap()));
        scrollEndCheckBox.addChangeListener(e -> textArea.setAutoscrolls(!textArea.getAutoscrolls()));
        slidingPanel.add("warp-line", warpLineCheckBox);
        slidingPanel.add("scroll-end", scrollEndCheckBox);*/

        JBScrollPane scrollPane = new JBScrollPane(textArea);
        scrollPane.setBackground(bg);

        scrollMenuItem.addActionListener(e -> {
            scrollPane.getViewport().setViewPosition(new Point(0, textArea.getHeight()));
            textArea.setAutoscrolls(true);
        });
        clearMenuItem.addActionListener(e -> textArea.setText(""));

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(scrollPane, "SQL Parser", false);
        toolWindow.getContentManager().addContent(content);
    }

}
