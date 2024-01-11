package io.github.arielcarrera.build.features.utils;

import org.gradle.api.Action;
import groovy.lang.Closure;

public class ActionClosure<T> extends Closure<Object> {
    private final Action<T> action;
    public ActionClosure(Object owner,Action<T> action) {
        super(owner);
        this.action = action;
    } 
    @SuppressWarnings("unchecked")
    @Override
    public Object call() {
        action.execute((T) getDelegate());
        return null;
    } 
}
