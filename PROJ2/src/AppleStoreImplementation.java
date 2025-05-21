import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.concurrent.*;

public class AppleStoreImplementation extends UnicastRemoteObject implements AppleStoreInterface {
    // Create a fixed-size thread pool with 5 threads
    private final ExecutorService executorService  = Executors.newFixedThreadPool(5);
    private final Map<String, Integer> appleStore = new ConcurrentHashMap<>();

    public AppleStoreImplementation() throws RemoteException{
        super();
    }


    /**
     * put key into the store
     * @param key
     * @param value
     * @throws RemoteException
     */
    @Override
    public void put(String key, int value) throws RemoteException {
        executorService.execute(() -> {
            appleStore.put(key, value);
            System.out.println("PUT " + key + ": " + value + " to the store.");
        });
    }

    /**
     * get result of corresponding key from the store
     * @param key
     * @return
     * @throws RemoteException
     */
    @Override
    public int get(String key) throws RemoteException {
        try {
            return executorService.submit(() -> appleStore.getOrDefault(key, 0)).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * delete key from the store
     * @param key
     * @throws RemoteException
     */
    @Override
    public void delete(String key, Integer value) throws RemoteException {
        executorService.execute(() -> {
            // use the appleStore object as a lock
            synchronized (appleStore) {
                // if it doesn't exist
                if (!appleStore.containsKey(key)) {
                    System.out.println("DELETE " + key + " does not exist.");
                    return;
                }

                // current number of the item
                int curQuantity = appleStore.get(key);
                int deleteAmount = (value == null) ? curQuantity: value; // if value is null, delete all of it
                // updated number of the item
                int newQuantity = curQuantity - deleteAmount;

                // if new number is <= 0, then remove all
                if (newQuantity <= 0){
                    appleStore.remove(key);
                    System.out.println("All of " + key + " have been removed.");
                }
                else{
                    System.out.println("Delete " + deleteAmount + " " + key + ".");
                    appleStore.put(key, newQuantity);
                }
            }

        });
    }

    /**
     * shut down the executor
     */
    public void shutdown() {
        executorService.shutdown();
        System.out.println("Executor service shutdown.");
    }
}
