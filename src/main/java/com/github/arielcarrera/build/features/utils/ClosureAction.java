package com.github.arielcarrera.build.features.utils;

import org.gradle.api.Action;
import groovy.lang.Closure;

public class ClosureAction<T> implements Action<T> {

    private final Closure<?> closure;

    public ClosureAction(Closure<?> closure) {
        this.closure = closure;
    }

    @Override
    public void execute(T delegate) {
        this.closure.setResolveStrategy(Closure.DELEGATE_FIRST);
        this.closure.setDelegate(delegate);
        if (this.closure.getMaximumNumberOfParameters() < 1) {
            this.closure.call();
            return;
        }
        this.closure.call(delegate);
    }

}