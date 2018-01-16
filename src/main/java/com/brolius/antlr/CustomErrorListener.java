package com.brolius.antlr;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class CustomErrorListener extends BaseErrorListener {
    public static CustomErrorListener INSTANCE = new CustomErrorListener();

    @Override
    public void syntaxError(Recognizer<?,?> recognizer, Object offSymb, int line, int charPos, String msg, RecognitionException e) {
        String sourceName = recognizer.getInputStream().getSourceName();
        if (!sourceName.isEmpty()) {
            sourceName = String.format("%s:%d:%d: ", sourceName, line, charPos);
        }

        System.out.println(sourceName+"line "+line+":"+charPos+ " "+msg);
    }
}
