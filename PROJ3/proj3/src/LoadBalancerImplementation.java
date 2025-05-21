import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LoadBalancerImplementation extends UnicastRemoteObject implements LoadBalancerInterface {
    private Registry registry;
    private final ArrayList<AppleStoreInterface> reps = new ArrayList<>();
    private int curIndex = 0;

    public LoadBalancerImplementation(Registry regis) throws RemoteException {
        super();
        this.registry = regis;
    }

    /** update the replicas in the registry
     *
     */
    public synchronized void updateReps() {
        reps.clear();
        try {
            // list all names in the registry
            String[] names = registry.list();

            for (String name : names) {
                if (name.startsWith("AppleStore")) {
                    try {
                        // look up the store name
                        AppleStoreInterface rep = (AppleStoreInterface) registry.lookup(name);

                        // check if it is active
                        if (rep.ping()) {
                            reps.add(rep);
                            System.out.println(name + " is found and added.");
                        }
                    }
                    catch (Exception e) {
                        System.out.println("Failed to connect: " + e.getMessage());
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Current replicas number: " + reps.size());
    }


    /**
     * put operation under two-phase
     * @param key
     * @param value
     * @throws RemoteException
     */
    @Override
    public synchronized String put(String key, int value) throws RemoteException {
        updateReps();

        // phase 1----prepare
        boolean allReady = true;
        for (AppleStoreInterface rep : reps) {
            try {
                boolean pre = rep.prePut(key, value);
                if (!pre) {
                    allReady = false;
                    break;
                }
            } catch (Exception e) {
                allReady = false;
                break;
            }
        }

        // phase 2----commit or abort

        // commit
        if (allReady) {
            for (AppleStoreInterface rep : reps) {
                try {
                    rep.commitPut(key, value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return "Situation: COMMITTED.";
        }

        // abort
        else{
            for (AppleStoreInterface rep : reps) {
                try {
                    rep.abortPut(key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return "Situation: ABORTED.";
        }

    }

    /**
     *
     * @param key
     * @return
     * @throws RemoteException
     */
    @Override
    public synchronized int get(String key) throws RemoteException {
        updateReps();
        AppleStoreInterface rep = NextRep();
        return (rep != null) ? rep.get(key): 0;
    }


    /**
     * from the list of replicas, select one AppleStore server in turn to handle the get() request
     * @return the selected AppleStore server
     */
    private synchronized AppleStoreInterface NextRep() {
        // check whether it is empty or not
        if (reps.isEmpty()){
            return null;
        }

        AppleStoreInterface curRep = reps.get(curIndex);
        curIndex = (curIndex + 1) % reps.size();
        return curRep;
    }

    /**
     * delete operation under two-phase
     * @param key
     * @param value
     * @return
     * @throws RemoteException
     */
    @Override
    public synchronized String delete(String key, Integer value) throws RemoteException {
        updateReps();
        // phase 1----prepare
        boolean allReady = true;
        for (AppleStoreInterface rep : reps) {
            try {
                boolean pre = rep.preDel(key, value);
                if (!pre) {
                    allReady = false;
                    break;
                }
            } catch (Exception e) {
                allReady = false;
                break;
            }
        }

        // phase 2----commit or abort

        // commit
        if (allReady) {
            for (AppleStoreInterface rep : reps) {
                try {
                    rep.commitDel(key, value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return "Situation: COMMITTED.";
        }

        // abort
        else{
            for (AppleStoreInterface rep : reps) {
                try {
                    rep.abortDel(key, value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return "Situation: ABORTED.";
        }

    }

    /**
     * get states of all replicas
     * @return
     * @throws RemoteException
     */
    @Override
    public  synchronized List<Map<String, Integer>> allRepsStates() throws RemoteException {
        updateReps();

        List<Map<String, Integer>> states = new ArrayList<>();
        for (AppleStoreInterface rep: reps){
            try{
                // store all data in the states
                states.add(rep.allData());
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        return states;
    }

}
