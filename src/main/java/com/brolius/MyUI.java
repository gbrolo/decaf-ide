package com.brolius;

import javax.print.PrintException;
import javax.servlet.annotation.WebServlet;

import com.brolius.antlr.CustomErrorListener;
import com.brolius.antlr.decafLexer;
import com.brolius.antlr.decafParser;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.*;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Window;
import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.Utils;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.Trees;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of an HTML page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
public class MyUI extends UI {
    private String editorInput;
    private ParseTree grammarParseTree;
    private decafParser grammarParser;
    private static final String endOfLine = "<br/>";
    private int level = 0;
    private String preetyFileTree;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        HorizontalLayout hLayout = new HorizontalLayout();
        hLayout.setSpacing(false);
        hLayout.setMargin(false);
        hLayout.setSizeFull();

        Label editorLbl = new Label("<strong>Code Editor</strong>", ContentMode.HTML);

        final VerticalLayout layout = new VerticalLayout();
        final VerticalLayout consolePanelLayout = new VerticalLayout();
        consolePanelLayout.setSpacing(false);

        final VerticalLayout consoleLayout = new VerticalLayout();

        Panel consolePanel = new Panel("Console");
        consolePanel.setHeight(500.0f, Unit.PIXELS);

        TextArea editor = new TextArea();
        editor.setSizeFull();
        editor.setWidth(100.0f, Unit.PERCENTAGE);
        editor.setHeight(420.0f, Unit.PIXELS);
        editor.addValueChangeListener(event -> {
           editorInput = String.valueOf(event.getValue());
        });

        Button button = new Button("Compile code");
        Button generateTreeBtn = new Button("Tree representation");
        Button clrConsoleBtn = new Button("Clear console");

        button.setSizeFull();
        generateTreeBtn.setSizeFull();
        clrConsoleBtn.setSizeFull();

        generateTreeBtn.setEnabled(false);

        button.addClickListener(e -> {
            if (editorInput != null) {
                consolePanelLayout.removeAllComponents();

                CharStream charStream = CharStreams.fromString(editorInput);
                decafLexer decafLexer = new decafLexer(charStream);
                decafLexer.removeErrorListeners();
                decafLexer.addErrorListener(new CustomErrorListener(consolePanelLayout));
                CommonTokenStream commonTokenStream = new CommonTokenStream(decafLexer);
                decafParser decafParser = new decafParser(commonTokenStream);
                decafParser.removeErrorListeners();
                decafParser.addErrorListener(new CustomErrorListener(consolePanelLayout));

                ParseTree parseTree = decafParser.program();
                grammarParseTree = parseTree;
                Label lbl1 = new Label("<strong>TREE>> </strong>" + parseTree.toStringTree(decafParser)
                        + "<br><i>For a cleaner tree, click the \"Tree representation \" button.<i>", ContentMode.HTML);
                lbl1.setWidth(100.0f, Sizeable.Unit.PERCENTAGE);
                consolePanelLayout.addComponent(lbl1);

                Notification notification = new Notification("Compilation done!", "Execution terminated!");
                notification.setDelayMsec(2000);
                notification.setPosition(Position.TOP_CENTER);
                notification.show(Page.getCurrent());

                grammarParser = decafParser;
                generateTreeBtn.setEnabled(true);
            } else {
                Notification notification = new Notification("Empty code", "The editor is empty",
                        Notification.Type.WARNING_MESSAGE, true);
                notification.setDelayMsec(4000);
                notification.setPosition(Position.BOTTOM_RIGHT);
                notification.show(Page.getCurrent());
            }

        });

        generateTreeBtn.addClickListener(event -> {
            TreeViewer viewer = new TreeViewer(Arrays.asList(grammarParser.getRuleNames()), grammarParseTree);
            viewer.setBorderColor(Color.WHITE);
            viewer.setBoxColor(Color.WHITE);
            try {
                viewer.save("tree.jpg");
                //generate indented tree
                List<String> ruleNamesList = Arrays.asList(grammarParser.getRuleNames());
                //System.out.println(ruleNamesList.toString());
                //System.out.println(prettyTree(parseTree, ruleNamesList));
                preetyFileTree = prettyTree(grammarParseTree, ruleNamesList);

                final Window window = new Window("Parse Tree");
                //window.setWidth(90.0f, Unit.PERCENTAGE);
                window.setHeight(90.0f, Unit.PERCENTAGE);
                window.center();
                window.setResizable(false);

                //String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
                FileResource resource = new FileResource(new File("tree.jpg"));
                Image image = new Image("Tree saved to /tree.jpg", resource);

                HorizontalLayout treeLayout = new HorizontalLayout();
                treeLayout.setMargin(true);
                treeLayout.setSpacing(true);
                treeLayout.setHeight(100.0f, Unit.PERCENTAGE);
                treeLayout.addComponent(image);
                treeLayout.setComponentAlignment(image, Alignment.MIDDLE_CENTER);

                //add Panel to layout
                Panel fileTreePanel = new Panel();
                fileTreePanel.setHeight(500.0f, Unit.PIXELS);

                Label fileTreeLbl = new Label(preetyFileTree, ContentMode.HTML);
                fileTreeLbl.setWidth(100.0f, Sizeable.Unit.PERCENTAGE);

                fileTreePanel.setContent(fileTreeLbl);
                treeLayout.addComponent(fileTreePanel);
                treeLayout.setComponentAlignment(fileTreePanel, Alignment.MIDDLE_LEFT);

                window.setContent(treeLayout);

                hLayout.getUI().getUI().addWindow(window);

                Notification notification = new Notification("Visual representation done!", "Click to dismiss");
                notification.setDelayMsec(500);
                notification.setPosition(Position.TOP_CENTER);
                notification.show(Page.getCurrent());

                generateTreeBtn.setEnabled(false);
            } catch (IOException e1) {
                e1.printStackTrace();
                Notification notification = new Notification("Failed tree visualization", e1.getMessage(),
                        Notification.Type.ERROR_MESSAGE, true);
                notification.setDelayMsec(4000);
                notification.setPosition(Position.BOTTOM_RIGHT);
                notification.show(Page.getCurrent());
            } catch (PrintException e1) {
                e1.printStackTrace();
                Notification notification = new Notification("Failed tree visualization", e1.getMessage(),
                        Notification.Type.ERROR_MESSAGE, true);
                notification.setDelayMsec(4000);
                notification.setPosition(Position.BOTTOM_RIGHT);
                notification.show(Page.getCurrent());
            }
        });

        clrConsoleBtn.addClickListener(event -> {
            consolePanelLayout.removeAllComponents();
        });

        HorizontalLayout editorButtonsLayout = new HorizontalLayout();
        editorButtonsLayout.setSizeFull();
        editorButtonsLayout.addComponents(button, generateTreeBtn, clrConsoleBtn);

        consolePanel.setContent(consolePanelLayout);
        consoleLayout.addComponent(consolePanel);
        layout.addComponents(editorLbl, editor, editorButtonsLayout);
        hLayout.addComponents(layout, consoleLayout);
        
        setContent(hLayout);
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }

    /**
     * Generates an indented string tree
     * Based on: https://github.com/grosenberg/SnippetsTest/blob/master/src/test/java/net/certiv/remark/test/TestBase.java
     * @param tree
     * @param rules
     * @return
     */
    public String prettyTree(final Tree tree, final List<String> rules) {
        level = 0;
        return process(tree, rules).replaceAll("(?m)^\\s+$", "").replaceAll("\\r?\\n\\r?\\n", endOfLine);
    }

    /**
     * The actual process to generate the indented tree
     * Based on: https://github.com/grosenberg/SnippetsTest/blob/master/src/test/java/net/certiv/remark/test/TestBase.java
     * @param tree
     * @param rules
     * @return
     */
    private String process(final Tree tree, final List<String> rules) {
        if (tree.getChildCount() == 0) return Utils.escapeWhitespace(Trees.getNodeText(tree, rules), false);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(lead(level));
        level++;
        String n = Utils.escapeWhitespace(Trees.getNodeText(tree, rules), false);
        stringBuilder.append(n + ' ');
        for (int i = 0; i < tree.getChildCount(); i++) {
            stringBuilder.append(process(tree.getChild(i), rules));
        }
        level--;
        stringBuilder.append(lead(level));
        return stringBuilder.toString();
    }

    /**
     * Adds indentations
     * Based on: https://github.com/grosenberg/SnippetsTest/blob/master/src/test/java/net/certiv/remark/test/TestBase.java
     * @param level
     * @return
     */
    private String lead(int level) {
        StringBuilder sb = new StringBuilder();
        if (level > 0) {
            sb.append(endOfLine);
            for (int cnt = 0; cnt < level; cnt++) {
                sb.append("--");
            }
        }
        return sb.toString();
    }
}
