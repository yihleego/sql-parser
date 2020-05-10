package io.leego.sql.parser;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Yihleego
 */
public class SQLParseAction extends AnAction {
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final String[] SQL_KEYWORDS = {"select ", "insert ", "update ", "delete ", "replace "};
    private static final String ARROW = "==>";
    private static final String PREPARING_PREFIX = "Preparing: ";
    private static final String PARAMETERS_PREFIX = "Parameters: ";
    private static final String COMMA = ",";
    private static final String COMMA_SPACE = ", ";
    private static final char OPEN_PARENTHESIS = '(';
    private static final char CLOSE_PARENTHESIS = ')';
    private static final char QUESTION_MARK = '?';
    private static final char SINGLE_QUOTATION_MARK = '\'';
    private static final char SEMICOLON = ';';
    private static final String QUESTION_MARK_REGEX = "\\?";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (null == editor) {
            return;
        }
        SelectionModel model = editor.getSelectionModel();
        final String selectedText = model.getSelectedText();
        if (selectedText == null) {
            return;
        }
        try {
            ToolWindow toolWindow = ToolWindowManager.getInstance(getEventProject(e)).getToolWindow("SQL Parser");
            if (toolWindow != null) {
                toolWindow.show(() -> {
                });
            }
            JBScrollPane scrollPane = (JBScrollPane) (toolWindow.getContentManager().getContent(0).getComponent());
            JBTextArea textArea = (JBTextArea) (scrollPane.getViewport().getView());
            textArea.append(dateTimeFormatter.format(LocalDateTime.now()) + "\n" + parse(selectedText));
        } catch (Exception ex) {
            Messages.showMessageDialog(ex.getMessage(), "Error", Messages.getErrorIcon());
        }
    }

    private String parse(String text) {
        String[] lines = text.split("\n|\r");
        if (lines.length < 2) {
            return text;
        }
        List<String> preparingList = new ArrayList<>();
        List<String> parametersList = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            String preparing = null;
            String parameters = null;
            String line = lines[i];
            String lineLowerCase = line.toLowerCase();
            for (String keyword : SQL_KEYWORDS) {
                if (lineLowerCase.contains(keyword)) {
                    preparing = line;
                    break;
                }
            }
            if (preparing != null) {
                while (++i < lines.length) {
                    if (isBlank(lines[i])) {
                        continue;
                    }
                    parameters = lines[i];
                    break;
                }
            }
            if (preparing != null && parameters != null) {
                preparingList.add(preparing);
                parametersList.add(parameters);
            }
        }
        if (preparingList.isEmpty()) {
            return "";
        }
        StringBuilder sql = new StringBuilder();
        for (int i = 0; i < preparingList.size(); i++) {
            String preparing = preparingList.get(i);
            String parameters = parametersList.get(i);
            // MyBatis log
            int preparingArrowIndex = preparing.indexOf(ARROW);
            if (preparingArrowIndex > -1) {
                preparing = preparing.substring(preparingArrowIndex + ARROW.length());
            }
            int preparingPrefixIndex = preparing.indexOf(PREPARING_PREFIX);
            if (preparingPrefixIndex > -1) {
                preparing = preparing.substring(preparingPrefixIndex + PREPARING_PREFIX.length());
            }
            int parametersArrowIndex = parameters.indexOf(ARROW);
            if (parametersArrowIndex > -1) {
                parameters = parameters.substring(parametersArrowIndex + ARROW.length());
            }
            int parametersPrefixIndex = parameters.indexOf(PARAMETERS_PREFIX);
            if (parametersPrefixIndex > -1) {
                parameters = parameters.substring(parametersPrefixIndex + PARAMETERS_PREFIX.length());
            }
            preparing = preparing.trim();
            parameters = parameters.trim();

            String[] params;
            String[] params1 = parameters.split(COMMA);
            String[] params2 = parameters.split(COMMA_SPACE);
            if (params2.length == params1.length) {
                params = params2;
            } else {
                params = params1;
            }
            int index = 0;
            while (preparing.indexOf(QUESTION_MARK) > 0 && index < params.length) {
                String param = params[index++];
                int openParenthesisIndex = param.indexOf(OPEN_PARENTHESIS);
                int closeParenthesisIndex = param.indexOf(CLOSE_PARENTHESIS);
                String trimmedParam;
                if (openParenthesisIndex > -1
                        && closeParenthesisIndex == param.length() - 1
                        && openParenthesisIndex < closeParenthesisIndex) {
                    trimmedParam = param.substring(0, openParenthesisIndex);
                } else {
                    trimmedParam = param;
                }
                preparing = preparing.replaceFirst(
                        QUESTION_MARK_REGEX,
                        SINGLE_QUOTATION_MARK + trimmedParam + SINGLE_QUOTATION_MARK);
            }
            sql.append(preparing);
            if (preparingList.size() > 1) {
                sql.append(SEMICOLON);
            }
            sql.append("\n\n");
        }
        return sql.toString();
    }

    public static boolean isEmpty(CharSequence text) {
        return text == null || text.length() == 0;
    }

    public static boolean isBlank(CharSequence text) {
        if (isEmpty(text)) {
            return true;
        }
        for (int i = 0, len = text.length(); i < len; ++i) {
            if (!Character.isWhitespace(text.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
