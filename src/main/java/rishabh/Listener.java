package rishabh;

import com.googlecode.lanterna.gui2.TextBox;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Listener implements Runnable {

    private DatagramSocket socket;
    private byte[] messageBuffer = new byte[2048];
    private final Scanner scanner = new Scanner(System.in);
    private Map<Integer, PeerInfo> peerList;
    public Set<String> peers;

    private LanternaUi gui;


    private PeerInfo selectedPeer;
    public int tcpServerPort;
    //at a time only one peer we can chat with;
    private boolean running;
    //for storing broadcast messages from peers;
    public int tcpClientPort;
    public Thread tcpClientHandlerThread; // make a setter for it and in this thread only check if the value is null or not;


    public void setChatThread(Thread obj) throws InterruptedException {
        this.tcpClientHandlerThread = obj;
        Thread.sleep(1500);
    }

    public void setSelectedPeer(PeerInfo selectedPeer) {
        this.selectedPeer = selectedPeer;
    }

    public Listener(int udpListenerPort, LanternaUi gui) throws SocketException, UnknownHostException {
        this.gui = gui;
        this.socket = new DatagramSocket(udpListenerPort, InetAddress.getByName("0.0.0.0"));
    }

    public void setPeerList(Map<Integer, PeerInfo> peerlist) {
        this.peerList = peerlist;
    }

    public void setPeers(Set<String> peers) {
        this.peers = peers;
    }


    private void sendToPeer(PeerInfo peer) throws IOException, InterruptedException {
        TcpClientSocket clientSocket = new TcpClientSocket(peer, peer.getTcpChatPort(), new TextBox(), gui);
        Thread tcpClientThread = new Thread(clientSocket);
        tcpClientThread.start();
        tcpClientThread.join();//when chatting with peer we don't do anything else on the main thread ;
        //main loop execution continues;
    }
    public int generateHash(int peerTcpPort, InetAddress peerAddr){//for the same entries the hash value will be the same;

        return Objects.hash(peerAddr, peerTcpPort);
    }


    @Override
    public void run() {
        //ok so this is the listener which listens for broadcast messages from other peers on the subnet

        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        PeerInvalidator peerInvalidator = new PeerInvalidator(peerList, gui);
        service.scheduleAtFixedRate(peerInvalidator, 5, 5, TimeUnit.SECONDS);

        running = true;
        gui.setPeerList(peerList);
        gui.setPeers((HashSet<String>) peers);
        Thread guiSetupThread = new Thread(() -> {
            try {
                gui.createLayout();//ok so the layout is ready now the internal lanterna gui thread has started;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        guiSetupThread.start();


        while (running) {
            //for recieving udp packets from peers;

            DatagramPacket message = new DatagramPacket(messageBuffer, messageBuffer.length);
            try {
                socket.receive(message);
                int senderTcpPort = ByteBuffer.wrap(messageBuffer).getInt();//the tcp chat port the peer sent in this udp packet;

                InetAddress senderAddr = message.getAddress();
                int senderPort = message.getPort(); // this is port from which the udp packet came;

                PeerInfo dupPeer = new PeerInfo("u", senderAddr, senderPort, senderTcpPort);
                int hash = generateHash(senderTcpPort, senderAddr);
                peerList.put(hash, dupPeer);
                gui.updatePeers();
            } catch (IOException e) {
                socket.close();
                throw new RuntimeException(e);
            }
        }
        socket.close();
    }


}
