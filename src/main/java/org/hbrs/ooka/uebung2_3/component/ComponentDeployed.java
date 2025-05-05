package org.hbrs.ooka.uebung2_3.component;

import java.lang.reflect.Method;

public class ComponentDeployed extends AbstractComponentState {

    @Override
    public void start(Component component) throws Exception {
        Method startMethod = component.getStartMethod();
        if (startMethod != null) {
            startMethod.setAccessible(true);
            Thread thread = new Thread(() -> {
                try {
                    startMethod.invoke(null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            thread.start();
            component.setThread(thread);
        }
    }

    @Override
    public void delete(Component component) {
        component.setComponentState(new ComponentDeleted());
    }

    @Override
    public ComponentState getState() {
        return ComponentState.DEPLOYED;
    }
}
