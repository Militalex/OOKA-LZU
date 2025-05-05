package org.hbrs.ooka.uebung2_3.component;

import java.lang.reflect.Method;

public class ComponentStarted extends AbstractComponentState {

    @Override
    public void stop(Component component, Thread thread) throws Exception {
        Method stopMethod = component.getStopMethod();
        if (stopMethod != null) {
            thread.interrupt();
            stopMethod.setAccessible(true);
            stopMethod.invoke(null);
        }
    }

    @Override
    public ComponentState getState() {
        return ComponentState.STARTED;
    }
}
