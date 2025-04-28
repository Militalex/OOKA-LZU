import org.hbrs.ooka.uebung2.runtimeEnvironment.RuntimeEnvironment;

import java.lang.reflect.InvocationTargetException;

public class TestMain {
    public static void main(String[] args) throws InterruptedException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        RuntimeEnvironment re = new RuntimeEnvironment("comps");
        re.start();

        re.getComponents().get("ProduktManagement Komponente").deploy(re);
        re.getComponents().get("ProduktManagement Client Simulation Komponente").deploy(re);

        re.getComponents().get("ProduktManagement Komponente").start();
        re.getComponents().get("ProduktManagement Client Simulation Komponente").start();

        Thread.sleep(1000);

        re.getComponents().get("ProduktManagement Client Simulation Komponente").stop();
        re.getComponents().get("ProduktManagement Komponente").stop();

        re.shutdown();
    }
}
