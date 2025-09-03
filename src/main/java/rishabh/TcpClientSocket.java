package rishabh;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class TcpClientSocket implements Runnable {


    //the guy who initially send the connection request to another peers server'
//but how does this guy listen for messages? and how does the guy who accepts send messages?
    private InetAddress peerIp;
    private int peerPort;
    private String msg;
    private Socket socket = null;
    private PrintWriter out;
    private BufferedReader in;
    private AtomicBoolean isChatting = new AtomicBoolean(true);
    private Scanner sc;
    private String peerName;

    private PrintWriter chatWriter = new PrintWriter(new FileWriter("chats.txt", true));


    public TcpClientSocket(PeerInfo peer, int tcpClientPort) throws IOException {
        this.peerIp = peer.getAddress();
        this.peerPort = tcpClientPort;
        this.sc = new Scanner(System.in); // right now testing from the same ip on diff. terminals;
        this.socket = new Socket();//this is the port i am sending the tcp socket connection request to but i should not know this beforehand i should get this port from the other peer himself but for now i am just testing;
        this.socket.connect(new InetSocketAddress(peerIp, peerPort));
        this.peerName = peer.getUsername(); // a random port for this socket is given by the os;
    }

    @Override
    public void run() {

        System.out.println("start of the chat with " + peerName);
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
            writer = new Writer(socket, out, in, isChatting);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Thread writerThread = new Thread(writer);
        writerThread.start(); // not joining coz i want the reader to keep reading the messages too


        String msg ="";
        while (isChatting.get()) {
            try {//jb writer socket close kr deta hai tb yha socket exception aajata hai because closed socket me se read krne ki koshish
                if ((msg = in.readLine()) == null) break;
            } catch (IOException e) {
                System.out.println("socket is closed");
                isChatting.set(false);
            }
            if(msg.startsWith("/end")){
                isChatting.set(false);
            }
            //            System.out.println(socket.getRemoteSocketAddress()+"-> remote  ->"+socket.getLocalAddress()+":"+socket.getLocalPort());
            System.out.println("[Peer]: " + msg);
        }
        System.out.println("clsoing client socket");

        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}