package org.hbrs.ooka.uebung2.component;

public class ComponentDeleted extends AbstractComponentState {

    @Override
    public ComponentState getState() {
        return ComponentState.DELETED;
    }
}
