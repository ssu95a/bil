package ru.inversion.bil;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;
import java.io.Reader;
import java.io.Writer;
import java.util.*;

public class FunctionContext extends SimpleScriptContext {

    static  private class ParentBindings implements Bindings {

        final private ScriptContext parent;

        private ParentBindings(ScriptContext parent) {
            this.parent = parent;
        }

        @Override
        public Object put(String name, Object value) { throw new UnsupportedOperationException("ParentBindings.put"); }

        @Override
        public void putAll(Map<? extends String, ?> toMerge) { throw new UnsupportedOperationException("ParentBindings.putAll"); }

        @Override
        public void clear() { }

        @Override
        public Set<String> keySet() { throw new UnsupportedOperationException("ParentBindings.keySet"); }

        @Override
        public Collection<Object> values() { throw new UnsupportedOperationException("ParentBindings.values"); }

        @Override
        public Set<Entry<String, Object>> entrySet() { throw new UnsupportedOperationException("ParentBindings.entrySet");  }

        @Override
        public int size() { throw new UnsupportedOperationException("ParentBindings.size(key)"); }

        @Override
        public boolean isEmpty() { return false; }

        @Override
        public boolean containsKey(Object key) {

            Bindings engineScope = parent.getBindings(ScriptContext.ENGINE_SCOPE);
            if (engineScope != null && engineScope.containsKey(key)) {
                return true;
            }

            Bindings globalScope = parent.getBindings(ScriptContext.GLOBAL_SCOPE);
            return globalScope != null && globalScope.containsKey(key);        }

        @Override
        public boolean containsValue(Object value) {
            throw new UnsupportedOperationException("ParentBindings.remove(key)");
        }

        @Override
        public Object get(Object key) {

            // 1. Сначала ENGINE_SCOPE (s, s1, s2, cnt)
            Bindings engineScope = parent.getBindings(ScriptContext.ENGINE_SCOPE);
            if (engineScope != null && engineScope.containsKey(key)) {
                return engineScope.get(key);
            }

            // 2. Потом GLOBAL_SCOPE (_FM, PoluchName, ACCA)
            Bindings globalScope = parent.getBindings(ScriptContext.GLOBAL_SCOPE);
            if (globalScope != null && globalScope.containsKey(key)) {
                return globalScope.get(key);
            }

            return null;        }

        @Override
        public Object remove(Object key) { throw new UnsupportedOperationException("ParentBindings.remove(key)"); }
    }

    final private ScriptContext  parent;
    final private ParentBindings bindings;
    /** */
    public FunctionContext( ScriptContext parent ) {
        this.parent   = parent;
        this.bindings = new ParentBindings( parent );
    }

    public ScriptContext getParent() {
        return parent;
    }

    @Override
    public void setBindings( Bindings bindings, int scope ) {
        throw new UnsupportedOperationException("setBindings");
    }

    @Override
    public Object getAttribute(String name) {

        if( super.engineScope.containsKey(name) )
            return super.getAttribute(name,ENGINE_SCOPE);

        return parent.getAttribute(name);
    }

    @Override
    public Object getAttribute(String name, int scope) {
        return getAttribute(name);
    }

    @Override
    public Object removeAttribute(String name, int scope) {
        return super.removeAttribute(name, scope);
    }

    @Override
    public void setAttribute(String name, Object value, int scope) {
        super.setAttribute(name, value, scope);
    }

    @Override
    public Writer getWriter() {
        return parent.getWriter();
    }

    @Override
    public Reader getReader() {
        return parent.getReader();
    }

    @Override
    public void setReader(Reader reader) {
    }

    @Override
    public void setWriter(Writer writer) {
    }

    @Override
    public Writer getErrorWriter() {
        return super.getErrorWriter();
    }

    @Override
    public void setErrorWriter(Writer writer) {
    }

    /** */
    @Override
    public int getAttributesScope(String name) {
        return parent.getAttributesScope(name);
    }

    /** */
    @Override
    public Bindings getBindings(int scope)
    {

        if( scope == ENGINE_SCOPE )
            return super.getBindings(scope);

        return bindings;
    }

    /** */
    @Override
    public List<Integer> getScopes() {
        return super.getScopes();
    }
}
