package rishabh;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class Writer implements Runnable{

    private Socket socket;

    //this is the main writer thread that takes some messages from the termianl and send to the socket endpoint;
    private PrintWriter out;
    private BufferedReader in;
    private Scanner sc;
    private AtomicBoolean isChatting;
    private PrintWriter chatWriter = new PrintWriter(new FileWriter("chats.txt", true), true);
    public Writer(Socket socket, PrintWriter out, BufferedReader in, AtomicBoolean isChatting) throws IOException {
        this.socket =socket;
        this.out = out;
        this.in = in;
        this.sc = new Scanner(System.in);


        this.isChatting = isChatting; //jb writer bna to socket hoga hi aur connection setup ho hi gya hoga/ isiliye writer close ho ja rha tha. kyuki ye false tha;

    }


    @Override
    public void run(){
        //now when the socket is recieved take client messages and send to the other socket;
        if(socket==null){
            return;
        }else{
            while(isChatting.get()){
                System.out.println("write: ");
                String input = sc.nextLine();//read from the terminal;//so mujhe yha se kuch liikhna padega uske bad hi writer close hoga because thread blocks at nextLine9);



                chatWriter.println("wrote: "+input);
                System.out.println("[me]: "+input);
                if(input.startsWith("/end")){
                    isChatting.set(false); //we also gotta signal the reader thread so that it stops trying to read input from the socket;
                }

                out.write(input+"\n");//auto flush is set
                out.flush();
                //i should see what i wrote right?

            } // so this writer thread is closing the socket too early for some reason;
            System.out.println("closing writer");

            try {
                socket.close();
                System.out.println("client socket is closed");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            finally {
                chatWriter.close();
            }


        }
    }

}
