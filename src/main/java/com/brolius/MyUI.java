package com.brolius;

import javax.servlet.annotation.WebServlet;

import com.brolius.antlr.CustomErrorListener;
import com.brolius.antlr.DecafErrorListener;
import com.brolius.antlr.decafLexer;
import com.brolius.antlr.decafParser;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.Position;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of an HTML page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
public class MyUI extends UI {
    String editorInput;

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
        editor.setHeight(300.0f, Unit.PIXELS);
        editor.addValueChangeListener(event -> {
           editorInput = String.valueOf(event.getValue());
        });

        Button button = new Button("Compile code");
        button.addClickListener(e -> {
            layout.addComponent(new Label(editorInput));

            CharStream charStream = CharStreams.fromString(editorInput);
            decafLexer decafLexer = new decafLexer(charStream);
            decafLexer.removeErrorListeners();
            decafLexer.addErrorListener(new CustomErrorListener(consolePanelLayout));
            CommonTokenStream commonTokenStream = new CommonTokenStream(decafLexer);
            decafParser decafParser = new decafParser(commonTokenStream);
            decafParser.removeErrorListeners();
            decafParser.addErrorListener(new CustomErrorListener(consolePanelLayout));

            ParseTree parseTree = decafParser.program();
            layout.addComponent(new Label(parseTree.toStringTree(decafParser)));

            Notification notification = new Notification("Compilation done!", "Execution terminated!");
            notification.setDelayMsec(2000);
            notification.setPosition(Position.BOTTOM_LEFT);
            notification.show(Page.getCurrent());

        });

        consolePanel.setContent(consolePanelLayout);
        consoleLayout.addComponent(consolePanel);
        layout.addComponents(editorLbl, editor, button);
        hLayout.addComponents(layout, consoleLayout);
        
        setContent(hLayout);
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
