package rishabh;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class TcpClientSocket implements Runnable {


    //the guy who initially send the connection request to another peers server'
//but how does this guy listen for messages? and how does the guy who accepts send messages?
    private InetAddress peerIp;
    private int peerPort;
    private String msg;
    private Socket socket = null;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isChatting;
    private Scanner sc;
    private String peerName;

    private PrintWriter chatWriter = new PrintWriter(new FileWriter("chats.txt", true));


    public TcpClientSocket(PeerInfo peer, int tcpClientPort, int localTcpPort) throws IOException {
        this.peerIp = peer.getAddress();
        this.peerPort = tcpClientPort;
        this.sc = new Scanner(System.in); // right now testing from the same ip on diff. terminals;
        this.socket = new Socket(peerIp, peerPort, null, localTcpPort);//this is the port i am sending the tcp socket connection request to but i should not this beforehand i should get this port from the other peer himself but for now i am just testing;
        this.isChatting = true;
        this.peerName = peer.getUsername();
    }

    @Override
    public void run() {

        System.out.println("start of the chat with " + peerIp);
        //can give the socket to the writer thread that can use the ip to send messages;
        //adn here ii only recieve meessages and display;

        try {
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Writer writer = null;
        try {
            writer = new Writer(socket, out, in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Thread writerThread = new Thread(writer);
        writerThread.start(); // not joining coz i want the reader to keep reading the messages too


        String msg;
        while (true) {
            try {
                if ((msg = in.readLine()) == null) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
//            System.out.println(socket.getRemoteSocketAddress()+"-> remote  ->"+socket.getLocalAddress()+":"+socket.getLocalPort());
            System.out.println("[Peer]: " + msg);
        }

        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}