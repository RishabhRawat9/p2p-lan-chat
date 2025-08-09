package rishabh;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Writer implements Runnable{

    private Socket socket;

    //this is the main writer thread that takes some messages from the termianl and send to the socket endpoint;
    private PrintWriter out;
    private BufferedReader in;
    private Scanner sc;
    private boolean isChatting;
    private PrintWriter chatWriter = new PrintWriter(new FileWriter("chats.txt", true), true);
    public Writer(Socket socket, PrintWriter out, BufferedReader in) throws IOException {
        this.socket =socket;
        this.out = out;
        this.in = in;
        this.sc = new Scanner(System.in);

        this.isChatting = true; //jb writer bna to socket hoga hi aur connection setup ho hi gya hoga/ isiliye writer close ho ja rha tha. kyuki ye false tha;

    }


    @Override
    public void run(){
        //now when the socket is recieved take client messages and send to the other socket;
        if(socket==null){
            return;
        }else{
            while(isChatting){
                System.out.println("write: ");
                String input = sc.nextLine();//read from the terminal;

                chatWriter.println("wrote: "+input);
                System.out.println("[me]: "+input);
                if(input.startsWith("/end")){
                    isChatting=false;
                }

                out.write(input+"\n");//auto flush is set
                out.flush();
                //i should see what i wrote right?

            } // so this writer thread is closing the socket too early for some reason;

            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            finally {
                chatWriter.close();
            }


        }
    }

}
