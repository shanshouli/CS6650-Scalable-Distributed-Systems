import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;

public class AppleStoreImplementation extends UnicastRemoteObject implements AppleStoreInterface {
    // Create a fixed-size thread pool with 5 threads
    private final ExecutorService executorService  = Executors.newFixedThreadPool(5);
    private final Map<String, Integer> appleStore = new ConcurrentHashMap<>();

    private final String repID;

//  private static final String[] repName = {"AppleStore1", "AppleStore2", "AppleStore3", "AppleStore4", "AppleStore5"};

    public AppleStoreImplementation(String repID) throws RemoteException{
        super();
        this.repID = repID;
    }


    /**
     * direct put operation from client
     * @param key
     * @param value
     * @throws RemoteException
     */
    @Override
    public String put(String key, int value) throws RemoteException {
        // it is not allowed to receive the request directly from the client
        System.out.println("Bad request. Replica" + repID + " received PUT request directly from the client.");
        return "Error happened. Please use Load Balancer.";
    }



    /**
     * get result of corresponding key from the store
     * @param key
     * @return
     * @throws RemoteException
     */
    public int get(String key) throws RemoteException {
        try {
            return executorService.submit(() -> appleStore.getOrDefault(key, 0)).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * direct delete operation from client
     * @param key
     * @throws RemoteException
     */
    @Override
    public String delete(String key, Integer value) throws RemoteException {
        System.out.println("Bad request. Participant" + repID + " received DELETE request directly from the client.");
        return "Error happened.";
    }

    /**
     * check if the server is active
     * @return
     * @throws RemoteException
     */
    @Override
    public boolean ping() throws RemoteException {
        return true;
    }

    /**
     * prepare to put
     * @param key
     * @param value
     * @return
     * @throws RemoteException
     */
    @Override
    public boolean prePut(String key, int value) throws RemoteException {
        System.out.println("Replica" + repID + " is preparing for PUT " + key + ": " + value);
        return true;
    }

    /**
     * commit phase for put operation
     * @param key
     * @param value
     * @throws RemoteException
     */
    @Override
    public void commitPut(String key, int value) throws RemoteException {
        executorService.execute(() -> {
            appleStore.put(key, value);
            System.out.println("Replica" + repID + " commited PUT " + key + ": " + value);
        });
    }

    /**
     * abort phase for put operation
     * @param key
     * @throws RemoteException
     */
    @Override
    public void abortPut(String key) throws RemoteException {
        System.out.println("Replica" + repID + " aborted PUT " + key);
    }

    /**
     * prepare to delete
     * @param key
     * @param value
     * @return
     * @throws RemoteException
     */
    @Override
    public boolean preDel(String key, Integer value) throws RemoteException {
        System.out.println("Replica" + repID + " is preparing for DELETE " + key + ": " + value);
        return true;
    }

    /**
     * commit for delete operation
     * @param key
     * @param value
     * @throws RemoteException
     */
    @Override
    public void commitDel(String key, Integer value) throws RemoteException {
        executorService.execute(() -> {
            // use the appleStore object as a lock
            synchronized (appleStore) {
                // if it doesn't exist
                if (!appleStore.containsKey(key)) {
                    System.out.println("Replica" + repID + "'s preparing for DELETE "+ key + " does not exist.");
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
                    System.out.println("Replica" + repID + ": All of " + key + " have been removed.");
                }
                else{
                    System.out.println("Replica" + repID + ": Delete " + deleteAmount + " " + key + ".");
                    appleStore.put(key, newQuantity);
                }
            }

        });
    }

    /**
     * abort delete operation
     * @param key
     * @param value
     * @throws RemoteException
     */
    @Override
    public void abortDel(String key, int value) throws RemoteException {
        System.out.println("Replica" + repID + " aborted DELETE " + key + ": " + value);
    }

    /**
     * return all data from current replica
     * @return
     * @throws RemoteException
     */
    @Override
    public Map<String, Integer> allData() throws RemoteException {
        return new ConcurrentHashMap<>(appleStore);
    }

    /**
     * shut down the executor
     */
    public void shutdown() {
        executorService.shutdown();
        System.out.println("Executor service shutdown.");
    }
}
