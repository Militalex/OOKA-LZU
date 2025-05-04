package org.hbrs.ooka.uebung2_3.component;

public class ComponentDeleted extends AbstractComponentState {

    @Override
    public ComponentState getState() {
        return ComponentState.DELETED;
    }
}
