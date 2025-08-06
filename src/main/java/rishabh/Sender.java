package rishabh;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;

public class Sender implements Runnable {


    private DatagramSocket senderSocket;
    private InetAddress address;
    private String msg = "from pc";

    private byte[] buff;

    public Sender() throws SocketException, UnknownHostException {
        this.buff = msg.getBytes();
        this.senderSocket = new DatagramSocket(9696);
        this.address =InetAddress.getByName("192.168.137.255"); // the wifi-6 broadcast ip;
        //now here the address would be the subnet address that i would need to calculate;
    }

    @Override
    public void run() {
        DatagramPacket packet = new DatagramPacket(buff,buff.length, address, 6969);
        try {
            senderSocket.send(packet);
            System.out.println("packet sent");
        } catch (IOException e) {
            senderSocket.close();
            throw new RuntimeException(e);
        }
        senderSocket.close();
    }


}
