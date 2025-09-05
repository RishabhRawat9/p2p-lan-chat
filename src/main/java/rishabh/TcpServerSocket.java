package rishabh;

import com.googlecode.lanterna.gui2.TextBox;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;

public class TcpServerSocket implements Runnable {
    private ServerSocket serverSocket;
    private int port;

    public ClientHandler clientHandler;
    private PrintWriter logging = new PrintWriter("log2.txt");

    public Listener listenerObj; //udp listener btw;
    private LanternaUi gui;

    private BufferedReader in;
    private TextBox messageArea;

    public TcpServerSocket(int port, ClientHandler clientHandler, Listener listenerObj, LanternaUi gui) throws FileNotFoundException {
        this.port = port;
        this.gui = gui;
        this.clientHandler = clientHandler;
        this.listenerObj = listenerObj;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept(); // blocked here;
                //now for the conn receiver peer you have to select him the peer aswell
                PeerInfo connectedPeer = new PeerInfo("peer", InetAddress.getByName("1.1.1.1"), clientSocket.getPort(), 00, Instant.now());
                gui.setSelectedPeer(connectedPeer);
                logging.append("connection received\n");
                logging.flush();
                gui.setPeerOutStream(new PrintWriter(clientSocket.getOutputStream()));
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                gui.updateStatus(clientSocket.getOutputStream().toString());
//                gui.updateStatus("chat connected with peer");
                //now this server can listen for messages as long as the clientsocket lives;
                String msg = "";
                while (true) {
                    try {//jb writer socket close kr deta hai tb yha socket exception aajata hai because closed socket me se read krne ki koshish
                        if ((msg = in.readLine()) == null) break;
                        else {
                            gui.updateMessage("[Peer]: " + msg);
                            logging.append("msg received: ").append(msg).append("\n");
                        }
                    } catch (IOException e) {
                        logging.append("socket is closed");
                        break;
                    }
                    if (msg.startsWith("/end")) {
                        clientSocket.close();
                        break;
                    }
                    logging.flush();
                }
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    logging.append("exception thrown server1");
                    logging.flush();
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                logging.append("execpetion thrown server 2");
                logging.flush();
                throw new RuntimeException(e);
            }
        }

    }


}