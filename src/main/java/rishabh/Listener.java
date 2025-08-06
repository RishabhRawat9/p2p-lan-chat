package rishabh;

import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Listener implements Runnable {

    private DatagramSocket socket;
    private byte[] messageBuffer = new byte[2048];


    public Set<String> clients; // after a client is added we migth need to start a tcp connection for chat aswell wil the chat be one on one or group?

    private boolean running;
    //for storing broadcast messages from peers;


    public Listener() throws SocketException, UnknownHostException {
        this.socket = new DatagramSocket(new InetSocketAddress("0.0.0.0", 6969));

        this.clients = new HashSet<>();
    }


    @Override
    public void run() {

        //ok so this is the listener which listens for broadcast messages from other peers on the subnet
        running = true;
        while (running) {
            DatagramPacket message = new DatagramPacket(messageBuffer, messageBuffer.length);
            try {
                System.out.println("server listeninng: " + socket.getLocalAddress() + " " + socket.getLocalPort());
                socket.receive(message);// blocks execution here until a message has arrived;

                //so from the packet retrieve the senders ip and port and then make a formatted string of the peer info and then store
                InetAddress senderAddr = message.getAddress();
                int senderPort = message.getPort();
                String entry = String.format("%s:%d", senderAddr, senderPort);
                clients.add(entry);
                System.out.println("client added");
                System.out.println(clients);
            } catch (IOException e) {
                socket.close();
                throw new RuntimeException(e);
            }
        }
        socket.close();
    }


}
