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
    public long ttl =10000;
    public long expireTime ;


    public PeerInfo(String username, InetAddress address, int port, int tcpChatPort) {
        this.username = username;
        this.address = address;
        this.port = port; // udp port from where the broadcast message came from not required.
        this.tcpChatPort = tcpChatPort;

        this.expireTime  = System.currentTimeMillis() + this.ttl;//the current time near about when the packet arrived;
        //storing the time when the peer should get expired i,.e curr time + ttl;
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
        return tcpChatPort == peerInfo.tcpChatPort && address.equals(peerInfo.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, tcpChatPort);
    }
}
