import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class server {
    public  static void main(String[] args){
        try {
            AppleStoreImplementation appleStore = new AppleStoreImplementation();
            // bind the registry with server
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("AppleStore", appleStore);
            System.out.println("AppleStore server is running.");

            // shut down the executor
            System.out.println("Press Enter to stop the server.");
            System.in.read();
            appleStore.shutdown();
            System.exit(0);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
