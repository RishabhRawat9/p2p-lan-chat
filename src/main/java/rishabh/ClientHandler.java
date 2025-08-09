package rishabh;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    public ClientHandler(){

    }
    public void setSocket(Socket clientSocket){
        this.clientSocket =clientSocket;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Writer writer = null;
        try {
            writer = new Writer(clientSocket, out,in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Thread writerThread = new Thread(writer);
        writerThread.start();

        String inputLine; // for the server connection the main loop thread still keeps running i need to block that aswell;



        //the main loop thread is supposed to wait until this client handler is being run;

        while (true) {
            try {
                if ((inputLine = in.readLine()) == null) break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if ("/end".equals(inputLine)) {
                break;
            }
            System.out.println("[peer]: "+ inputLine);
        }
//for the server socket i need to block the mainloop as well o/w the connection acceptor guy would get invalid sometimes as the main loop thread would start executing;

        try {
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        out.close();
        try {
            clientSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}