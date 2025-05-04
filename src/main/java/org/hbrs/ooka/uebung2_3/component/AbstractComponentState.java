package org.hbrs.ooka.uebung2_3.component;

import org.hbrs.ooka.uebung2_3.runtimeEnvironment.RuntimeEnvironment;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public abstract class AbstractComponentState {
    public @Nullable Method[] deploy(Component component, RuntimeEnvironment re, int id) throws Exception{
        throw new ComponentStateUnsuportedOperationException("Deploying is not supported in State " + getState());
    }

    public void start(Component component) throws Exception{
        throw new ComponentStateUnsuportedOperationException("Starting is not supported in State " + getState());
    }

    public void stop(Component component) throws Exception{
        throw new ComponentStateUnsuportedOperationException("Stopping is not supported in State " + getState());
    }

    public void delete(Component component) {
        throw new ComponentStateUnsuportedOperationException("Deleting is not supported in State " + getState());
    }

    public abstract ComponentState getState();
}
