package org.hbrs.ooka.uebung2.services.logger;

import org.hbrs.ooka.uebung2.component.Component;

public final class ComponentLogger extends AbstractLogger{
    public ComponentLogger(Component component, int id) {
        super(component.getName() + "::" + id);
    }
}
