package rishabh;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;

public class Sender implements Runnable {



    private DatagramSocket senderSocket;
    private InetAddress address;
    private int sendPort;
    private byte[] buff;

    public Sender(int udpbroadcastport, int sendPort, int tcpServerPort) throws SocketException, UnknownHostException {
        this.buff = ByteBuffer.allocate(4).putInt(tcpServerPort).array(); //ab msg me har peer apna chat server ka port bhejega taki dusra banda
        //iss port pe tcp connection ki request bhej ske;
        this.sendPort = sendPort;
        this.senderSocket = new DatagramSocket(udpbroadcastport);//binding udp socket to this port

        this.address =InetAddress.getByName("172.21.31.255"); // the wifi-6 broadcast ip;test from wsl

        //now here the address would be the subnet address that i would need to calculate;
    }

    @Override
    public void run() {
        DatagramPacket packet = new DatagramPacket(buff,buff.length, address, sendPort);
        //packet goes to this address and port;

        try {
            for(int j=0;j<10;j++){
                senderSocket.send(packet);
//                System.out.println("packet sent");
                Thread.sleep(5000);
            }
        } catch (IOException e) {
            senderSocket.close();
            throw new RuntimeException(e);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        senderSocket.close();
    }


}
