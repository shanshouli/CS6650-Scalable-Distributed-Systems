import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;

public class client {
    public static void main(String[] args){
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            LoadBalancerInterface appleStore = (LoadBalancerInterface) registry.lookup("LoadBalancer");

            // pre-populate the key-value store
            System.out.println("Pre-populate the key-value store.\n");
            appleStore.put("MacBook", 200);
            appleStore.put("AppleWatch", 500);
            appleStore.put("iPad", 300);
            appleStore.put("VisionPro", 50);
            appleStore.put("HomePod", 100);

            // wait to make sure the previous operation is done
            Thread.sleep(1000);

            // Delete the data from the key-value store
            System.out.println("Delete the data from the key-value store.\n");
            appleStore.delete("MacBook", 200);
            appleStore.delete("AppleWatch", 200);
            appleStore.delete("iPad", null);
            appleStore.delete("VisionPro", 100);
            appleStore.delete("HomePod", 30);

            // wait to make sure the previous operation is done
            Thread.sleep(1000);

            // Retrieve the data from the key-value store
            System.out.println("Retrieve the data from the key-value store.\n");
            System.out.println("MacBook: " + appleStore.get("MacBook"));
            System.out.println("AppleWatch: " + appleStore.get("AppleWatch"));
            System.out.println("iPad: " + appleStore.get("iPad"));
            System.out.println("VisionPro: " + appleStore.get("VisionPro"));
            System.out.println("HomePod: " + appleStore.get("HomePod"));


            // Check whether concurrent threads work
            System.out.println("Running two threads.\n");

            // thread 1 updated the number of HomePods
            Thread t1 = new Thread(() -> {
                try{
                    appleStore.put("HomePod", 300);
                    System.out.println("Thread 1 get HomePod: " + appleStore.get("HomePod"));
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            });

            // thread 2 updated the number of HomePods
            Thread t2 = new Thread(() -> {
                try{
                    appleStore.put("HomePod", 200);
                    System.out.println("Thread 2 get HomePod: " + appleStore.get("HomePod"));
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            });

            // start two threads
            t1.start();
            t2.start();

            // wait for two threads to finish
            t1.join();
            t2.join();

            Thread.sleep(500);

            // final check
            List<Map<String, Integer>> states = appleStore.allRepsStates();
            for (int i = 0; i < states.size(); i++){
                System.out.println("Rep " + (i + 1) + ": " + states.get(i));
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
