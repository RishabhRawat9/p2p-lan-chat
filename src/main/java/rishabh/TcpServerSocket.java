package rishabh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServerSocket implements Runnable {
    private ServerSocket serverSocket;

    private int port;

    public ClientHandler clientHandler;

    public Listener listenerObj; //udp listener btw;

    public TcpServerSocket(int port, ClientHandler clientHandler, Listener listenerObj){
        this.port = port;
        this.clientHandler = clientHandler;
        this.listenerObj = listenerObj;

    }

    @Override
    public void run()  {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept(); // blocked here;
//                this.clientHandler.setSocket(clientSocket);
                System.out.println("connection request came");

                //now is the time to block the mainloop thread and continue it when the chat is done or one of the clients closes the scoket and i alsoe need to handle that gracefullly lke when one part closes the chat no errors are thrown i just go back to the mainloop thread;

                Thread clientThread = new Thread(new ClientHandler(clientSocket));//jo main me iska obj bna that usiko reuse kre hai diff. threads me;
                listenerObj.setChatThread(clientThread);//now here this value is set and in the listener i can join() this thread so that the litener thread execution is blocked until the chatThread is not done;
                clientThread.start(); // will need to spawn new threads every time a reuqest comes in;
                clientThread.join();
                listenerObj.setChatThread(null);//like after its done then i don't need to keep refernceing so that gc can collect it
                System.out.println("chat over : server listening for connecctions");
                //the server would keep running only the client handler thread is closed;
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }



}