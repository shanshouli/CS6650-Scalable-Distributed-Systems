import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface LoadBalancerInterface extends Remote {
    /**
     * put the item into the store
     * @param key
     * @param value
     * @throws RemoteException
     */
    String put(String key, int value) throws RemoteException;

    /**
     * retrieve the number of item from the store
     * @param key
     * @return
     * @throws RemoteException
     */
    int get(String key) throws RemoteException;

    /**
     * delete item from the store
     * @param key
     * @throws RemoteException
     */
    String delete(String key, Integer value) throws RemoteException;

    /**
     * get states of all replicas
     * @return
     * @throws RemoteException
     */
    List<Map<String, Integer>> allRepsStates() throws  RemoteException;

}
