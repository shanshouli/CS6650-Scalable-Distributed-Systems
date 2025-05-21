import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AppleStoreInterface extends Remote{
    /**
     * put the item into the store
     * @param key
     * @param value
     * @throws RemoteException
     */
    void put(String key, int value) throws RemoteException;

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
    void delete(String key, Integer value) throws RemoteException;
}
