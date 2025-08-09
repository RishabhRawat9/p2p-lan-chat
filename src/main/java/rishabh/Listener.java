package rishabh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Listener implements Runnable {

    private DatagramSocket socket;
    private byte[] messageBuffer = new byte[2048];
    private final Scanner scanner = new Scanner(System.in);
    private final Map<Integer, PeerInfo> peerList = new ConcurrentHashMap<>();
    public Set<String> peers = new HashSet<>();
    private PeerInfo selectedPeer;
    public int tcpServerPort;


    //at a time only one peer we can chat with;

    public Set<String> clients; // after a client is added we migth need to start a tcp connection for chat aswell wil the chat be one on one or group?

    private boolean running;
    //for storing broadcast messages from peers;
    public int tcpClientPort;
    public Thread tcpClientHandlerThread; // make a setter for it and in this thread only check if the value is null or not;


    public void setChatThread(Thread obj) throws InterruptedException {
        this.tcpClientHandlerThread = obj;
        Thread.sleep(1500);
    }


    public Listener(int udpListenerPort, int tcpClientPort, int tcpServerPort) throws SocketException, UnknownHostException {
        this.socket = new DatagramSocket(new InetSocketAddress("0.0.0.0", udpListenerPort));
        this.tcpClientPort = tcpClientPort;
        this.tcpServerPort = tcpServerPort;

    }


    private void sendToPeer(PeerInfo peer) throws IOException, InterruptedException {

        TcpClientSocket clientSocket = new TcpClientSocket(peer, tcpClientPort, tcpServerPort);
        Thread tcpClientThread = new Thread(clientSocket);
        tcpClientThread.start();
        tcpClientThread.join();//when chatting with peer we don't do anything else on the main thread ;
    }


    @Override
    public void run() {
        //ok so this is the listener which listens for broadcast messages from other peers on the subnet
        running = true;
        startInputLoop();
        while (running) {
            DatagramPacket message = new DatagramPacket(messageBuffer, messageBuffer.length);
            try {
//                System.out.println("server listeninng: " + socket.getLocalAddress() + " " + socket.getLocalPort());
                socket.receive(message);// blocks execution here until a message has arrived;

                //so from the packet retrieve the senders ip and port and then make a formatted string of the peer info and then store
                InetAddress senderAddr = message.getAddress();

                int senderPort = message.getPort();
                String entry = String.format("%s:%d", senderAddr, senderPort);//for now hardcoding the values;

                peers.add(entry);

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
                    if (tcpClientHandlerThread != null) {
                        tcpClientHandlerThread.join();
                        System.out.println("client handler joined");//aabe join hone ke bad thodi print hoga unless end hogya ho
                    }
                    else{
                        System.out.println("client handler null");
                    }
                    // now siince this is a obj if the state of the arg cahnges then join() works;

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
//                System.out.print("> ");
                String input = scanner.nextLine().trim();

                if (input.startsWith("/select")) {
                    try {
                        handleSelect();
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    }
                } else if (input.startsWith("/peers")) {
                    try {
                        showPeers();
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    }
                } else if (input.startsWith("/chat")) {
                    System.out.println("chatting with " + selectedPeer.toString());
                    try {
                        sendToPeer(selectedPeer);
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else if (input.startsWith("/quit")) {
                    System.out.println("Exiting...");
                    System.exit(0);
                } else {
                    System.out.println("invalid");
                }
            }
        }).start();
    }

    private void showPeers() throws UnknownHostException {
        int j = 1;
        for (String peer : peers) {
            String addr[] = peer.split(":");
            String host = addr[0];
            host = host.replaceFirst("^/", "");
            peerList.put(j++, new PeerInfo("user", InetAddress.getByName(host), Integer.parseInt(addr[1])));
        }
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
