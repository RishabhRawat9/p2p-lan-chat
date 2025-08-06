package rishabh;

import java.net.InetAddress;

public class PeerInfo {
    private final String username;
    private final InetAddress address;
    private final int port;

    public PeerInfo(String username, InetAddress address, int port) {
        this.username = username;
        this.address = address;
        this.port = port;
    }

    public String getUsername() { return username; }
    public InetAddress getAddress() { return address; }
    public int getPort() { return port; }

    @Override
    public String toString() {
        return username + " (" + address.getHostAddress() + ":" + port + ")";
    }
}
