package rishabh;

import java.net.SocketException;
import java.net.UnknownHostException;

public class App {
    public static void main(String[] args) throws SocketException, UnknownHostException {
        Sender sender = new Sender();
        Listener listener = new Listener();


//        new Thread(sender).start();
        new Thread(listener).start();
    }
}
