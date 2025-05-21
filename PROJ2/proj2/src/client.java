import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class client {
    public static void main(String[] args){
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            AppleStoreInterface appleStore = (AppleStoreInterface) registry.lookup("AppleStore");

            // pre-populate the key-value store
            System.out.println("Pre-populate the key-value store.\n");
            appleStore.put("MacBook", 200);
            appleStore.put("AppleWatch", 500);
            appleStore.put("iPad", 300);
            appleStore.put("VisionPro", 50);
            appleStore.put("HomePod", 100);

            // wait to make sure the previous operation is done
            Thread.sleep(1000);

            // Retrieve the data from the key-value store
            System.out.println("Retrieve the data from the key-value store.\n");
            System.out.println("MacBook: " + appleStore.get("MacBook"));
            System.out.println("AppleWatch: " + appleStore.get("AppleWatch"));
            System.out.println("iPad: " + appleStore.get("iPad"));
            System.out.println("VisionPro: " + appleStore.get("VisionPro"));
            System.out.println("HomePod: " + appleStore.get("HomePod"));


            // Delete the data from the key-value store
            System.out.println("Delete the data from the key-value store.\n");
            appleStore.delete("MacBook", 200);
            appleStore.delete("AppleWatch", 200);
            appleStore.delete("iPad", null);
            appleStore.delete("VisionPro", 100);
            appleStore.delete("HomePod", 30);

            // wait to make sure the previous operation is done
            Thread.sleep(1000);

            // Check whether deletion operation works
            System.out.println("After deletion, what is left in the store.\n");
            System.out.println("MacBook: " + appleStore.get("MacBook"));
            System.out.println("AppleWatch: " + appleStore.get("AppleWatch"));
            System.out.println("iPad: " + appleStore.get("iPad"));
            System.out.println("VisionPro: " + appleStore.get("VisionPro"));
            System.out.println("HomePod: " + appleStore.get("HomePod"));
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
