package org.hbrs.ooka.uebung2.component;

public abstract class ComponentState {

    public abstract void deploy();

    public abstract void start();

    public abstract void stop();

    public abstract void delete();
    public abstract String getState();
}
