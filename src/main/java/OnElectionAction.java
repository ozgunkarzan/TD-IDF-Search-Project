import cluster.management.OnElectionCallback;
import cluster.management.ServiceRegistry;
import networking.WebClient;
import networking.WebServer;
import org.apache.zookeeper.KeeperException;
import search.SearchCoordinator;
import search.SearchWorker;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class OnElectionAction implements OnElectionCallback {
    private  final ServiceRegistry workersServiceRegistry;
    private  final ServiceRegistry coordinatorsServiceRegistry;
    private final int port;
    private WebServer webServer;

    public OnElectionAction(ServiceRegistry workersServiceRegistry, ServiceRegistry coordinatorsServiceRegistry, int port) {
        this.workersServiceRegistry = workersServiceRegistry;
        this.coordinatorsServiceRegistry = coordinatorsServiceRegistry;
        this.port = port;
    }

    @Override
    public void onElectedToBeLeader() {
        try {
            workersServiceRegistry.unregiterFromCluster();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        }
        workersServiceRegistry.registerForUpdates();

        if(webServer != null){
            webServer.stop();
        }
        SearchCoordinator searchCoordinator= new SearchCoordinator(workersServiceRegistry, new WebClient());
        webServer=new WebServer(port, searchCoordinator);
        try {
            webServer.startServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
           String currentServerAddress = String.format("http://%s:%d%s", InetAddress.getLocalHost()
                   .getCanonicalHostName(),port, searchCoordinator.getEndpoint());
           coordinatorsServiceRegistry.registerToCluster(currentServerAddress);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public void onWorker() {
        SearchWorker searchWorker= new SearchWorker();
        webServer=new WebServer(port, searchWorker);
        try {
            webServer.startServer();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            String currentServerAddress= String.format("http://%s:%d%s", InetAddress.getLocalHost().getCanonicalHostName(),port,searchWorker.getEndpoint());
       workersServiceRegistry.registerToCluster(currentServerAddress);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        }

    }
}
