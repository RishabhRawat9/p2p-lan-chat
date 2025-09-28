package rishabh;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PeerInvalidator implements Runnable{
    Map<Integer, PeerInfo> peerList;
    LanternaUi gui;
    public PeerInvalidator(Map<Integer, PeerInfo> peerList, LanternaUi gui){
        this.peerList =peerList;
        this.gui = gui;
    }
    @Override
    public void run() {
        peerList.values().removeIf(peer -> System.currentTimeMillis() > peer.expireTime);
        gui.updatePeers();

    }
}
