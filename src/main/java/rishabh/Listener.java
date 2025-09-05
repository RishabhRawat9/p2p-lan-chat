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
import java.util.stream.Collectors;

public class Listener implements Runnable {

    private DatagramSocket socket;
    private byte[] messageBuffer = new byte[2048];
    private final Scanner scanner = new Scanner(System.in);
    private Map<Integer, PeerInfo> peerList;
    public Set<String> peers;

    private LanternaUi gui ;


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

    public void setSelectedPeer(PeerInfo selectedPeer){
        this.selectedPeer = selectedPeer;
    }

    public Listener(int udpListenerPort, LanternaUi gui) throws SocketException, UnknownHostException {
        this.gui =gui;
        this.socket = new DatagramSocket(udpListenerPort, InetAddress.getByName("0.0.0.0"));
    }

    public void setPeerList(Map<Integer, PeerInfo> peerlist) {
        this.peerList = peerlist;
    }

    public void setPeers(Set<String> peers) {
        this.peers = peers;
    }


    private void sendToPeer(PeerInfo peer) throws IOException, InterruptedException {
        TcpClientSocket clientSocket = new TcpClientSocket(peer, peer.getTcpChatPort(),new TextBox(), gui);
        Thread tcpClientThread = new Thread(clientSocket);
        tcpClientThread.start();
        tcpClientThread.join();//when chatting with peer we don't do anything else on the main thread ;
        //main loop execution continues;
    }


    @Override
    public void run() {
        //ok so this is the listener which listens for broadcast messages from other peers on the subnet
        running = true;
        gui.setPeerList(peerList);
        gui.setPeers((HashSet<String>) peers);
        Thread guiSetupThread = new Thread(() -> {
            try {
                gui.createLayout();//ok so the layout is ready now the internal lanterna gui thread has started;
                //first of all it should display the list of active pe3ers;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        guiSetupThread.start();


        while (running) {
            //for recieving packets;

            DatagramPacket message = new DatagramPacket(messageBuffer, messageBuffer.length);
            try {
                socket.receive(message);// blocks execution here until a message has arrived;
                int senderTcpPort = ByteBuffer.wrap(messageBuffer).getInt();//the tcp chat port.
                //now the peerPort is in the message parse it and when making new peerinfo obj set it as tcpchatport;
                InetAddress senderAddr = message.getAddress();
                int senderPort = message.getPort(); // this is port from which the udp packet came;
                String entry = String.format("%s:%d:%d", senderAddr, senderPort, senderTcpPort);//for now hardcoding the values;
                int size = peers.size();
                peers.add(entry);
                PeerInfo dupPeer= new PeerInfo("u", senderAddr, senderPort, senderTcpPort, Instant.now());
                if(peers.size()!=size){
                    peerList.put(peerList.size()+1, dupPeer);
                }
                //go over all the peers and see which one was it and then update the time?//not efficient fix later;
                else{
                    peerList.forEach((id, peer)->{
                        if(peer.equals(dupPeer)){
                            peer.lastPacketTime = Instant.now();
                        }
                    });
                }
                gui.updatePeers();
            } catch (IOException e) {
                socket.close();
                throw new RuntimeException(e);
            }
        }
        socket.close();
    }
    private void startInputLoop() {
        new Thread(() -> {
            while (true) {
                try {
                    if (tcpClientHandlerThread != null && tcpClientHandlerThread.isAlive()) {
                        tcpClientHandlerThread.join();
                        //if some chat is going on then this thread is blcoke;d

                        System.out.println("client handler joined");//aabe join hone ke bad thodi print hoga unless end hogya ho
                    } else {
                        System.out.println("client handler null");
                    }
                    // now siince this is a obj if the state of the arg cahnges then join() works;

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                String input = scanner.nextLine().trim();


            }
        }).start();
    }

    private void showPeers() throws UnknownHostException {
        int j = 1;
        for (String peer : peers) {
            String[] addr = peer.split(":");
            String host = addr[0];
            host = host.replaceFirst("^/", "");
//            peerList.put(j++, new PeerInfo("user", InetAddress.getByName(host), Integer.parseInt(addr[1]), Integer.parseInt(String.valueOf(addr[2]))));
        }//building from peers set but how do you invalidate peers that have gone offline but still remain in the peers set;

        System.out.println("Available peers:");
        System.out.println(peerList);
        System.out.println(peers);
        peerList.forEach((id, peer) -> System.out.println("[" + id + "] " + peer));
    }

    private void handleSelect() throws UnknownHostException {
        showPeers();
        System.out.print("Enter peer number to select");
        String line = scanner.nextLine();
        try {
            int id = Integer.parseInt(line.trim());
            if (peerList.containsKey(id)) {
                selectedPeer = peerList.get(id);
            } else {
                System.out.println("Invalid ID: " + id);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input: " + line);
        }
    }


}
