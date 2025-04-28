package org.hbrs.ooka.uebung2.component;

public class ComponentStopped extends AbstractComponentState {

    @Override
    public void delete(Component component) {
        component.setComponentState(new ComponentDeleted());
    }

    @Override
    public ComponentState getState() {
        return ComponentState.STOPPED;
    }
}
