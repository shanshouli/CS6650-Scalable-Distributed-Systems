import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface AppleStoreInterface extends Remote{

    /**
     * PUT operation
     * @param key
     * @param value
     * @return
     * @throws RemoteException
     */
    String put(String key, int value) throws RemoteException;

    /**
     * GET operation
     * @param key
     * @return
     * @throws RemoteException
     */
    int get(String key) throws RemoteException;

    /**
     * DELETE operation
     * @param key
     * @param value
     * @return
     * @throws RemoteException
     */
    String delete(String key, Integer value) throws RemoteException;

    /**
     * Check whether server is active
     * @return
     * @throws RemoteException
     */
    boolean ping() throws RemoteException;

    /**
     * prepare for put operation
     * @param key
     * @param value
     * @return
     * @throws RemoteException
     */
    boolean prePut(String key, int value) throws RemoteException;

    /**
     * commit for put operation
     * @param key
     * @param value
     * @throws RemoteException
     */
    void commitPut(String key, int value) throws RemoteException;

    /**
     * abort a put operation
     * @param key
     * @throws RemoteException
     */
    void abortPut(String key) throws RemoteException;

    /**
     * prepare for put operation
     * @param key
     * @param value
     * @return
     * @throws RemoteException
     */
    boolean preDel(String key, Integer value) throws RemoteException;

    /**
     * commit for put operation
     * @param key
     * @param value
     * @throws RemoteException
     */
    void commitDel(String key, Integer value) throws RemoteException;

    /**
     * abort a put operation
     * @param key
     * @param value
     * @throws RemoteException
     */
    void abortDel(String key, int value) throws RemoteException;

    /**
     * get all data from current replica
     * @return
     * @throws RemoteException
     */
    Map<String, Integer> allData() throws RemoteException;

}
