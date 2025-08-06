package rishabh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class TcpClientSocket implements Runnable {

    private InetAddress peerIp;
    private int peerPort;
    private String msg;
    private Socket socket = null;
    private OutputStream out;
    private InputStream in;
    private boolean isChatting;
    private Scanner sc;
    private String peerName;
    public TcpClientSocket(PeerInfo peer) throws IOException {
        this.peerIp = peer.getAddress();
        this.peerPort = 8080;
        this.sc = new Scanner(System.in);
        this.socket = new Socket(peerIp, peerPort);
        this.isChatting = true;
        this.peerName = peer.getUsername();

    }

    @Override
    public void run() {
        try (
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {
            System.out.println("start of the chat with "+ peerIp);
            while(isChatting){
                System.out.print("["+peerName+"]: ");
                String msg = sc.nextLine();
                if(msg.startsWith("/end")){
                   break;
                }
                out.write((msg+"\n").getBytes()); // ont he tcp server end the output was displayed together because the readline kept reading until it found \n so after every input adding a \n to kkeep the messages real time;
                out.flush();
            }
            socket.close();
            System.out.println("thread closed");
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            System.err.println("Error during TCP communication: " + e.getMessage());
        }
    }
}