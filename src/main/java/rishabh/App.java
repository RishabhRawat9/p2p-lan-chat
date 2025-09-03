package rishabh;

import java.net.SocketException;
import java.net.UnknownHostException;

public class App {
    public static void main(String[] args) throws SocketException, UnknownHostException, InterruptedException {

        LanternaUi gui = new LanternaUi();
        try {
            gui.createLayout();
        } catch (Exception e) {
            // TODO: handle exception
        }

        // udp
        int udpBroadCastPort = Integer.parseInt(args[0]); // iss port se packets bhejenge
        int udpListenPort = Integer.parseInt(args[1]);
        int sendBroadCastPort = Integer.parseInt(args[2]); // sending udp packets to this port;
        // passing the broadcast on other device to this port;
        int tcpServerPort = Integer.parseInt(args[3]);
        Sender sender = new Sender(udpBroadCastPort, sendBroadCastPort, tcpServerPort);
        //
        // int tcpPort = Integer.parseInt(args[2]); // sending connection request to
        // this port;

        ClientHandler tcpClientHandler = new ClientHandler();// hasn't started yet;

        Listener listener = new Listener(udpListenPort);
        TcpServerSocket tcpServer = new TcpServerSocket(tcpServerPort, tcpClientHandler, listener);
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
