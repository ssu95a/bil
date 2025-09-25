package ru.inversion.bil;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import java.util.Arrays;
import java.util.List;

public class BilScriptEngineFactory implements ScriptEngineFactory {
    
    @Override
    public String getEngineName() {
        return "Bil Script Engine";
    }

    @Override
    public String getEngineVersion() {
        return "1.0";
    }

    @Override
    public List<String> getExtensions() {
        return Arrays.asList("bil");
    }

    @Override
    public List<String> getMimeTypes() {
        return Arrays.asList("text/x-bil", "application/x-bil");
    }

    @Override
    public List<String> getNames() {
        return Arrays.asList("bil", "Bil");
    }

    @Override
    public String getLanguageName() {
        return "Bil";
    }

    @Override
    public String getLanguageVersion() {
        return "1.0";
    }

    @Override
    public Object getParameter(String key) {
        switch (key) {
            case ScriptEngine.ENGINE: return getEngineName();
            case ScriptEngine.ENGINE_VERSION: return getEngineVersion();
            case ScriptEngine.NAME: return getNames().get(0);
            case ScriptEngine.LANGUAGE: return getLanguageName();
            case ScriptEngine.LANGUAGE_VERSION: return getLanguageVersion();
            case "THREADING": return "MULTITHREADED";
            default: return null;
        }
    }

    @Override
    public String getMethodCallSyntax(String obj, String method, String... args) {
        StringBuilder sb = new StringBuilder();
        sb.append(obj).append(".").append(method).append("(");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(args[i]);
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String getOutputStatement(String toDisplay) {
        return "print(" + toDisplay + ");";
    }

    @Override
    public String getProgram(String... statements) {
        StringBuilder sb = new StringBuilder();
        for (String stmt : statements) {
            sb.append(stmt).append(";\n");
        }
        return sb.toString();
    }

    @Override
    public ScriptEngine getScriptEngine() {
        return new BilScriptEngine(this);
    }
}