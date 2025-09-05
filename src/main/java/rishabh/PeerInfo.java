package rishabh;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Objects;

public class PeerInfo {
    private final String username;
    private final InetAddress address;
    private final int port; // ye port udp broadcast wala port hoga dusre peer ka;
    private int tcpChatPort;
    public Instant lastPacketTime;

    public PeerInfo(String username, InetAddress address, int port, int tcpChatPort, Instant lastPacketTime) {
        this.username = username;
        this.address = address;
        this.port = port; // udp port from where the broadcast message came from not required.
        this.tcpChatPort = tcpChatPort;
        this.lastPacketTime =lastPacketTime;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeerInfo peerInfo = (PeerInfo) o;
        return port == peerInfo.port && address.equals(peerInfo.address);
    }
}
