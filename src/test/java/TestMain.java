import org.hbrs.ooka.uebung2.runtimeEnvironment.RuntimeEnvironment;

public class TestMain {
    public static void main(String[] args) throws InterruptedException {
        RuntimeEnvironment re = new RuntimeEnvironment("comps");
        re.start();

        re.listComponents();

        re.deployComponentById(1);
        re.deployComponentById(0);
        re.deployComponentById(0);

        re.listComponents();

        re.startComponentById(1);
        re.startComponentById(0);

        Thread.sleep(500);

        re.listComponents();

        Thread.sleep(5000);

        re.shutdown();
    }
}
