package rishabh;

import com.googlecode.lanterna.gui2.TextBox;
import org.w3c.dom.Text;

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
    private PrintWriter logging = new PrintWriter("log.txt");




    // if the server socket opens another socket for handling the client;
    private Socket clientSocket;
    private PrintWriter clientOut;
    private BufferedReader clientIn;



    private TextBox inputBox;
    private LanternaUi gui;
    public TcpClientSocket(PeerInfo peer, int tcpClientPort, TextBox inputBox, LanternaUi gui) throws IOException {
        this.peerIp = peer.getAddress();
        this.peerPort = tcpClientPort;
        this.gui = gui;
        this.inputBox = inputBox;
        this.sc = new Scanner(System.in); // right now testing from the same ip on diff. terminals;
        this.socket = new Socket();//this is the port i am sending the tcp socket connection request to but i should not know this beforehand i should get this port from the other peer himself but for now i am just testing;
        logging.println(peerPort);
        logging.flush();
        this.socket.connect(new InetSocketAddress(peerIp, peerPort));

        this.peerName = peer.getUsername(); // a random port for this socket is given by the os;
    }

    public TcpClientSocket(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.clientOut = new PrintWriter(clientSocket.getOutputStream());
        this.clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public PrintWriter getSocketOutStream(){
        return out;
    }
    @Override
    public void run() {

//        System.out.println("start of the chat with " + peerName);
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
        String msg ="";
        while (true) {
            try {//jb writer socket close kr deta hai tb yha socket exception aajata hai because closed socket me se read krne ki koshish
                if ((msg = in.readLine()) == null) break;
            } catch (IOException e) {
                isChatting.set(false);
            }
            if(msg.startsWith("/end")){
                isChatting.set(false);
                gui.updateStatus("chat over");
                break;
            }
            gui.updateMessage("[Peer]: "+ msg);
            logging.append(msg);
            logging.flush();
        }

        try {
            socket.close();
        } catch (IOException e) {
            logging.append(e.toString());
            logging.flush();

        }
    }
}