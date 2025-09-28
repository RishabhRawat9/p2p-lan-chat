package rishabh;

import java.io.FileNotFoundException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class App {

    private static LanternaUi gui;

    static {
        try {
            gui = new LanternaUi();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Map<Integer, PeerInfo> peerList = new ConcurrentHashMap<>();
    public static Set<String> peers = new HashSet<>(); // this ain't thread safe; but not shared b/w multiple things;

    public static void main(String[] args) throws SocketException, UnknownHostException, InterruptedException, FileNotFoundException{


        // udp
        int udpBroadCastPort = 0; // iss port se packets bhejenge
        int udpListenPort = Integer.parseInt(args[0]);

        int sendBroadCastPort = Integer.parseInt(args[1]); // sending udp packets to this port;
        // passing the broadcast on other device to this port;
        int tcpServerPort = Integer.parseInt(args[2]);
        Sender sender = new Sender(udpBroadCastPort, sendBroadCastPort, tcpServerPort);


        Listener listener = new Listener(udpListenPort, gui);

        listener.setPeers(peers);
        listener.setPeerList(peerList);

        TcpServerSocket tcpServer = new TcpServerSocket(tcpServerPort, listener, gui);
        Thread serverThread = new Thread(tcpServer);
        serverThread.start();

        Thread lTHread = new Thread(listener);
        Thread sThread = new Thread(sender);
        sThread.start();
        lTHread.start();
        sThread.join();
        lTHread.join();
        serverThread.join();


    }
}