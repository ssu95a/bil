package ru.inversion.bil;

import javax.script.ScriptContext;
import java.io.IOException;
import java.util.Arrays;

public class FormMaker {

    private final ScriptContext context;

    /** */
    public FormMaker( ScriptContext context ) {
        this.context = context;
    }

    public void print( String text ) {
        try {
            context.getWriter().write(text);
            context.getWriter().flush();
        } catch (IOException e) {
            throw new RuntimeException("Error in FormMaker.print: " + e.getMessage(), e);
        }
    }

    /** */
    public void println(String text) {
        try {
            context.getWriter().write(text);
            context.getWriter().write("\n");
            context.getWriter().flush();
        } catch (IOException e) {
            throw new RuntimeException("Error in FormMaker.println: " + e.getMessage(), e);
        }
    }

    /** */
    public void line() {
        try {
            context.getWriter().write("----------------------------------------\n");
            context.getWriter().flush();
        } catch (IOException e) {
            throw new RuntimeException("Error in FormMaker.line: " + e.getMessage(), e);
        }
    }

    public void report(String... messages) {
        try {
            context.getWriter().write("=== REPORT ===\n");
            for (String message : messages) {
                context.getWriter().write("• " + message + "\n");
            }
            context.getWriter().write("==============\n");
            context.getWriter().flush();
        } catch (IOException e) {
            throw new RuntimeException("Error in FormMaker.report: " + e.getMessage(), e);
        }
    }

    // Метод для вызова из скрипта с любыми аргументами
    public void report(Object... objects) {
        try {
            context.getWriter().write("=== REPORT ===\n");
            for (Object obj : objects) {
                context.getWriter().write("• " + (obj != null ? obj.toString() : "null") + "\n");
            }
            context.getWriter().write("==============\n");
            context.getWriter().flush();
        } catch (IOException e) {
            throw new RuntimeException("Error in FormMaker.report: " + e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return "FormMaker instance";
    }
}