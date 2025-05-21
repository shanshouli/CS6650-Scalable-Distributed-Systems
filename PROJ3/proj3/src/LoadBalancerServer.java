import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class LoadBalancerServer {
    public static void main(String[] args){
        try{

            // create the location of RMI registry on port 1099
            Registry registry = LocateRegistry.createRegistry(1099);

            LoadBalancerImplementation lb = new LoadBalancerImplementation(registry);

            // bind the load balancer with a name in the RMI registry
            registry.rebind("LoadBalancer", lb);
            System.out.println("Load Balancer has started working.");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
