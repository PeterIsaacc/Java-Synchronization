import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

class Router {
    private int maxDevices;
    private int currentCount = 0;
    private ArrayList<Device> devices;
    private Semaphore fullSem;
    private Semaphore emptySem;
    private static Router instance;


    public static Router getInstance(int v) {
        if (instance == null) {
            instance = new Router(v);
        }

        return instance;
    }

    private Router(int maxDevices) {
        this.maxDevices = maxDevices;
        devices = new ArrayList<Device>();
        emptySem = new Semaphore(maxDevices);
        fullSem = new Semaphore(0);
    }

    public void login(Device device) throws IOException {
        if (emptySem.value - 1 < 0) {
            Network.logs(device.name + " is Waiting...");
            System.out.println(device.name + " is Waiting...");
        }
        emptySem.wait_();
        if (currentCount < maxDevices) {
            devices.add(device);
            currentCount++;
        }
        String message = ("(" + device.name + ") (" + device.type + ") logged in" + (char) 9 + (char) 9 + "<--");
        Network.logs(message);
        System.out.println(message);
        fullSem.signal();
    }

    public void logout(Device device) throws IOException {
        fullSem.wait_();
        devices.remove(device);
        currentCount--;
        String message = ("(" + device.name + ") (" + device.type + ") logged out" + (char) 9 + (char) 9 + "-->");
        Network.logs(message);
        System.out.println(message);
        emptySem.signal();
    }

}


class Semaphore {
    public int value = 0;

    public Semaphore(int v) {
        value = v;
    }

    public synchronized void wait_() {
        value--;
        if (value < 0)
            try {
                wait();
            } catch (InterruptedException e) {
            }
    }

    public synchronized void signal() {
        value++;
        if (value <= 0) notify();
    }
}


class Device extends Thread {
    public String name = "";
    public String type = "";
    Router router = Router.getInstance(-1); //Value is useless here because it will be already made using the correct number

    public Device(String name, String type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public void run() {
        try {
            router.login(this);
            Thread.sleep(1000);
            Network.logs(name + " is browsing the web");
            System.out.println(name + " is browsing the web");
            Thread.sleep(3000);

            router.logout(this);
        } catch (InterruptedException | IOException e) {
            try {
                router.logout(this);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

    }
}


class Network {
    static final String log_directory = System.getProperty("user.home") + File.separator + "Ass2_Log.txt";
    static final File fileCreator = new File(log_directory);
    static FileWriter file;

    public static synchronized void logs(String message) throws IOException {
        Network.file = new FileWriter(log_directory, true);
        Network.file.write(message + "\n");
        Network.file.close();

    }

    public static void main(String[] args) throws IOException {
        if (!fileCreator.exists()) {
            fileCreator.createNewFile();
        }
        Scanner scanner = new Scanner(System.in);
        int maxDevices = 2;
        Device devices[];

        System.out.println("What is the maximum number of Connections?");
        maxDevices = scanner.nextInt();
        //predefined scenario for quick testing
        if (maxDevices < 1) {
            System.out.println("your entry is not valid...");
            System.out.println("executing a predefined scenario");
            predefinedScenario();
            return;
        }
        Router router = Router.getInstance(maxDevices);

        System.out.println("What is the number of devices Clients want to connect?");
        int num = 4;
        num = scanner.nextInt();
        devices = new Device[num];
        for (int i = 1; i <= num; i++) {
            System.out.println("Enter data of device number: " + i);
            if (i == 1)
                scanner.nextLine();

            String input = scanner.nextLine();
            String s[] = (input + " useless_text_to_fix_split_issues").split(" ");
            devices[i - 1] = new Device(s[0], s[1]);
        }

        for (Device device : devices)
            device.start();

    }

    public static void predefinedScenario() {
        Router router = Router.getInstance(2);
        Device devices[] = new Device[7];

        devices[0] = new Device("c1", "mobile");
        devices[1] = new Device("c2", "tablet");
        devices[2] = new Device("c3", "pc");
        devices[3] = new Device("c4", "pc");
        devices[4] = new Device("c5", "Smart Fridge");
        devices[5] = new Device("c6", "mobile");
        devices[6] = new Device("c7", "pc");

        for (Device device : devices)
            device.start();
    }

}
