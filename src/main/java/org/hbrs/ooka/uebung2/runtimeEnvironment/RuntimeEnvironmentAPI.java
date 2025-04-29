package org.hbrs.ooka.uebung2.runtimeEnvironment;

import org.hbrs.ooka.uebung2.component.Component;
import org.hbrs.ooka.uebung3.runtimeEnvironment.IRuntimeEnvironmentAPI;

import java.util.HashMap;

/**
 * API of the RuntimeEnvironment with Dispatching functionality.
 * TODO Refactor
 */
public class RuntimeEnvironmentAPI implements IRuntimeEnvironmentAPI {

    private HashMap<Component, String> componentToPortString = new HashMap<>();
    private HashMap<String, Object> ports = new HashMap<>();
    private HashMap<String, Integer> amounts = new HashMap<>();
    private HashMap<String, Integer> nextDispatches = new HashMap<>();

    public void addPort(Component component, Object port) {
       String key = port.getClass().getName();
       if (amounts.containsKey(key)) {
           amounts.put(key, amounts.remove(key) + 1);
       }
       else {
           amounts.put(key, 0);
           nextDispatches.put(key, 0);
       }
       key = key + "_" + amounts.get(key);
       componentToPortString.put(component, key);
       ports.put(key, port);
    }

    //TODO Component Port Removal

    @Override
    public Object getPort(Class<?> portClass) {
        int amount = amounts.get(portClass.getName());
        int dispatch = nextDispatches.get(portClass.getName());
        nextDispatches.put(portClass.getName(), (dispatch + 1) % (amount + 1));
        return ports.get(portClass.getName() + "_" + dispatch);
    }
}
