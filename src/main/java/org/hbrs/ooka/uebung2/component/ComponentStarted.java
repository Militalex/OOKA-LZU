package org.hbrs.ooka.uebung2.component;

import java.lang.reflect.Method;

public class ComponentStarted extends AbstractComponentState {

    @Override
    public void stop(Component component) throws Exception {
        Method stopMethod = component.getStopMethod();
        if (stopMethod != null) {
            stopMethod.setAccessible(true);
            stopMethod.invoke(null);
        }
    }

    @Override
    public ComponentState getState() {
        return ComponentState.STARTED;
    }
}
