package org.hbrs.ooka.uebung2_3.runtimeEnvironment;

import lombok.AccessLevel;
import lombok.Getter;
import org.hbrs.ooka.uebung2_3.component.Component;
import org.hbrs.ooka.uebung2_3.component.ComponentState;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Getter(AccessLevel.PACKAGE)
public class RuntimeEnvironmentConfig {
    private final List<String> components;
    private final List<String> loadedOrDeployed;

    public RuntimeEnvironmentConfig(Collection<Component> components) throws IOException {
        this.components = components.stream().map(Component::getFullName).toList();
        this.loadedOrDeployed = components.stream().map(component -> {
            ComponentState state = component.getState();
            return ((state == ComponentState.LOADED) ? state : ComponentState.DEPLOYED).toString();
        }).toList();
    }
}
