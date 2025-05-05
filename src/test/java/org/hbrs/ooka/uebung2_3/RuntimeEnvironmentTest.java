package org.hbrs.ooka.uebung2_3;

import org.hbrs.ooka.uebung2_3.component.Component;
import org.hbrs.ooka.uebung2_3.runtimeEnvironment.RuntimeEnvironment;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class RuntimeEnvironmentTest {

    @Test
    public void separateClassLoaderTest() throws Exception {
        Component component1 = new Component("comps/ProduktManagementSimulation-COMPONENT.jar");
        Component component2 = new Component(component1);

        Constructor<?> constructor1 = component1.getClassLoader().loadClass("org.hbrs.ooka.uebung1.component.Product")
                .getDeclaredConstructor(int.class, String.class, double.class);
        Constructor<?> constructor2 = component2.getClassLoader().loadClass("org.hbrs.ooka.uebung1.component.Product")
                .getDeclaredConstructor(int.class, String.class, double.class);

        Object product1a = constructor1.newInstance(1, "Beispiel Produkt", 10.0);
        Object product1b = constructor1.newInstance(1, "Beispiel Produkt", 10.0);

        Object product2a = constructor2.newInstance(1, "Beispiel Produkt", 10.0);
        Object product2b= constructor2.newInstance(1, "Beispiel Produkt", 10.0);

        // Die Objekte sollten identisch sein
        assertEquals(product1a, product1b);
        assertEquals(product2a, product2b);

        // Die Objekte zwar identisch sollten aber aufgrund von verschiedenen ClassLoaders nicht gleich sein
        assertNotEquals(product1a, product2a);
        assertNotEquals(product1b, product2b);
    }

    @Test
    public void roundTripTest() throws Exception {
        RuntimeEnvironment re = new RuntimeEnvironment();
        re.start();
        re.loadAllComponents("comps");

        re.listComponents();

        re.deployComponentById(0);
        re.deployComponentById(1);
        re.deployComponentById(1);

        re.listComponents();

        re.startComponentById(0);
        re.startComponentById(1);

        Thread.sleep(500);

        re.listComponents();

        Thread.sleep(5000);

        re.shutdown();

        System.out.println("-----------------");

        re.start();
        re.loadConfig("config/LZU-config.json");
        re.listComponents();
        re.shutdown();
    }
}
