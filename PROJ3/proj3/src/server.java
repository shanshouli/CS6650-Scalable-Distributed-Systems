import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class server {
    public  static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        String repID = args.length > 0 ? args[0] : "1";

        try {
            // connect to the existing RMI registry
            Registry registry = LocateRegistry.getRegistry(1099);

            // if it is registered, ask user to re-enter
            while (repID == null || existedRep(registry, repID)){
                if (repID != null){
                    System.out.println("repID: " + repID + "has been registered." );
                }
                System.out.println("Please enter a new replicaID: ");
                repID = scanner.nextLine().trim();
            }

            // create an instance
            AppleStoreImplementation appleStore = new AppleStoreImplementation(repID);

            // register new instance in the registry
            registry.rebind("AppleStore" + repID, appleStore);
            System.out.println("AppleStore" + repID + " is running.");

            // shut down the executor
            System.out.println("Press Enter to stop the server.");
            System.in.read();
            appleStore.shutdown();
            System.exit(0);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally{
            scanner.close();
        }

    }

    /**
     * helper function to check whether the server name has existed or not
     * @param registry
     * @param repID
     * @return
     */
    private static boolean existedRep(Registry registry, String repID){
        try {
            registry.lookup("AppleStore" + repID);
            // it has been registered
            return true;
        } catch (NotBoundException e){
            // it is not registered
            return false;
        } catch (Exception e){
            e.printStackTrace();
            return true;
        }
    }
}
