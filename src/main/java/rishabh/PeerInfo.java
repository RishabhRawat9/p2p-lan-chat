package rishabh;

import java.net.InetAddress;

public class PeerInfo {
    private final String username;
    private final InetAddress address;
    private final int port; // ye port udp broadcast wala port hoga dusre peer ka;
    private int tcpChatPort;

    public PeerInfo(String username, InetAddress address, int port, int tcpChatPort) {
        this.username = username;
        this.address = address;
        this.port = port;
        this.tcpChatPort = tcpChatPort;
    }
    public int getTcpChatPort(){
        return tcpChatPort;
    }

    public String getUsername() { return username; }
    public InetAddress getAddress() { return address; }
    public int getPort() { return port; }

    @Override
    public String toString() {
        return username + " (" + address.getHostAddress() + ":" + port + ")";
    }
}
