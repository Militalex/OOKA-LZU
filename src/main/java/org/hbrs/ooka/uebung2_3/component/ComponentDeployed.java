package org.hbrs.ooka.uebung2_3.component;

import java.lang.reflect.Method;

public class ComponentDeployed extends AbstractComponentState {

    @Override
    public void start(Component component) throws Exception {
        Method startMethod = component.getStartMethod();
        if (startMethod != null) {
            startMethod.setAccessible(true);
            startMethod.invoke(null);
        }
    }

    @Override
    public ComponentState getState() {
        return ComponentState.DEPLOYED;
    }
}
