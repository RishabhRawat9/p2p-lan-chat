package rishabh;

import java.net.SocketException;
import java.net.UnknownHostException;

public class App {
    public static void main(String[] args) throws SocketException, UnknownHostException, InterruptedException {

        int udpBroadCastPort = Integer.parseInt(args[0]);
        int udpListenPort = Integer.parseInt(args[1]);
        int tcpPort = Integer.parseInt(args[2]); // sending connection request to this port;
        int sendBroadCastPort = Integer.parseInt(args[3]); // sending udp packets to this port;
        int tcpServerPort = Integer.parseInt(args[4]);
        int localTcpPort = Integer.parseInt(args[5]);

        ClientHandler tcpClientHandler = new ClientHandler();//hasn't started yet;


        Sender sender = new Sender(udpBroadCastPort, sendBroadCastPort);

        Listener listener = new Listener(udpListenPort, tcpPort, localTcpPort);
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
