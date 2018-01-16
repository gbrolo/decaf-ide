package com.brolius;

import javax.servlet.annotation.WebServlet;

import com.brolius.antlr.CustomErrorListener;
import com.brolius.antlr.DecafErrorListener;
import com.brolius.antlr.decafLexer;
import com.brolius.antlr.decafParser;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
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
        final VerticalLayout layout = new VerticalLayout();

        TextArea editor = new TextArea();
        editor.setSizeFull();
        editor.addValueChangeListener(event -> {
           editorInput = String.valueOf(event.getValue());
        });

        Button button = new Button("Click Me");
        button.addClickListener(e -> {
            layout.addComponent(new Label(editorInput));

            CharStream charStream = CharStreams.fromString(editorInput);
            decafLexer decafLexer = new decafLexer(charStream);
            decafLexer.removeErrorListeners();
            decafLexer.addErrorListener(CustomErrorListener.INSTANCE);
            CommonTokenStream commonTokenStream = new CommonTokenStream(decafLexer);
            decafParser decafParser = new decafParser(commonTokenStream);
            decafParser.removeErrorListeners();
            decafParser.addErrorListener(CustomErrorListener.INSTANCE);

            ParseTree parseTree = decafParser.program();
            layout.addComponent(new Label(parseTree.toStringTree(decafParser)));

        });
        
        layout.addComponents(editor, button);
        
        setContent(layout);
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
