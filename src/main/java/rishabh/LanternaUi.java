package rishabh;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Component;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.TerminalSize;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LanternaUi {
    private PrintWriter logging = new PrintWriter("log.txt");
    private MultiWindowTextGUI gui;
    private BasicWindow window;
    private TextBox messageArea;
    private TextBox inputBox;
    private Panel peersPanel;
    private Label statusLabel;
    private Button sendButton;


    private PeerInfo selectedPeer;

    private Map<Integer, PeerInfo> peerList;
    private HashSet<String> peers;
    private PrintWriter peerOutStream; // the connected peers socket output stream;

    public LanternaUi() throws FileNotFoundException {
    }

    public void setPeerList(Map<Integer, PeerInfo> peerlist) {
        this.peerList = peerlist;
    }

    public void setPeers(HashSet<String> peers) {
        this.peers = peers;
    }

    public void setPeerOutStream(PrintWriter peerOutStream) {
        this.peerOutStream = peerOutStream;

    }
    public void setSelectedPeer(PeerInfo selectedPeer){
        this.selectedPeer = selectedPeer;
    }

    public TextBox getMessageArea() {
        return messageArea;
    }


    private volatile boolean running = true;

    public void createLayout() throws IOException {
        Terminal terminal = new DefaultTerminalFactory().createTerminal();
        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();

        gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));

        window = new BasicWindow();
        window.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.FIT_TERMINAL_WINDOW));
        window.setTitle("P2P Messenger");

        Panel contentPanel = new Panel();
        contentPanel.setLayoutManager(new BorderLayout());

        // Status bar at top
        statusLabel = new Label("Status: Starting...");
        statusLabel.setForegroundColor(TextColor.ANSI.GREEN);
        contentPanel.addComponent(statusLabel, BorderLayout.Location.TOP);

        // Main content area
        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

        // Message area (left side)
        Panel messagePanel = new Panel();
        messagePanel.setLayoutManager(new BorderLayout());
        messagePanel.addComponent(new Label("Messages:"), BorderLayout.Location.TOP);

        messageArea = new TextBox("MEssaes appear here if everything works fine", TextBox.Style.MULTI_LINE);
        messageArea.setReadOnly(true);
        messageArea.setPreferredSize(new TerminalSize(60, 20));
        messagePanel.addComponent(messageArea, BorderLayout.Location.CENTER);

        // Input area at bottom of messages
        Panel inputPanel = new Panel();
        inputPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
        inputPanel.addComponent(new Label("Type message:"));


        inputBox = new TextBox();
        inputBox.setPreferredSize(new TerminalSize(60, 3));

        inputPanel.addComponent(inputBox);
        sendButton = new Button("Send", () -> {
            String message = inputBox.getText();
            logging.append("button triggered\n");

            parseMessage(message);

            if (!message.trim().isEmpty() && selectedPeer != null) {
                sendMessage(message); //message has to go to the selected peer right;

            } else {
                updateStatus("select a peer first");
            }
            inputBox.setText("");
            logging.flush();
        });
        inputPanel.addComponent(sendButton, BorderLayout.Location.RIGHT);

        messagePanel.addComponent(inputPanel, BorderLayout.Location.BOTTOM);
        mainPanel.addComponent(messagePanel);


        Panel rightPanel = new Panel();
        rightPanel.setLayoutManager(new BorderLayout());
        rightPanel.addComponent(new Label("Connected Peers:"), BorderLayout.Location.TOP);

        peersPanel = new Panel();
        peersPanel.setPreferredSize(new TerminalSize(30, 5));
        peersPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
        rightPanel.addComponent(peersPanel, BorderLayout.Location.CENTER);

        mainPanel.addComponent(rightPanel);
        contentPanel.addComponent(mainPanel, BorderLayout.Location.CENTER);

        window.setComponent(contentPanel);
        gui.addWindowAndWait(window);
    }


    public void updateStatus(String status) {
        if (statusLabel != null) {
            gui.getGUIThread().invokeLater(() -> {
                statusLabel.setText("Status: " + status);
            });
        }
    }

    public void updateMessage(String message) { // now the sender , listener , client handler needs to call this whenver they wanna dispaly /update a message onto the scrren;

        if (messageArea != null) {
            gui.getGUIThread().invokeLater(() -> {
                String currentText = messageArea.getText();
                messageArea.setText(currentText + (currentText.isEmpty() ? "" : "\n") + message);
            });
        }
    }

    public void updatePeers() { // to invalidate peers i would need to keep checking the time for each of them continuously
        if (peersPanel != null) { // i say fuck that for now let's just finish integrating the ui;
            gui.getGUIThread().invokeLater(() -> {
                peersPanel.removeAllComponents();
                peerList.forEach((id, peer) -> {
                    Label peerLabel = new Label("[" + id + " " + peer.getPort() + "] ");
                    peersPanel.addComponent(peerLabel);
                });
                peersPanel.invalidate();

            });
        }
    }

    private void sendMessage(String message) {

        String displayMessage =  message;
        // ihave the out stream now i can send the messages;
        if (peerOutStream != null) {
            peerOutStream.write(displayMessage + "\n"); //tcp stream needs to end with \n or \r\n so that readline can know the line has ended o/w it just keeps reading that's why the other guy was not seeing anything.
            logging.append(displayMessage);

            peerOutStream.flush();
            updateMessage("[YOU]: "+ displayMessage);
        } else {
            logging.append("no peer connected\n");

        }
        //alright so the connection is setup now with the peer all i need is the input and output socket stream.
    }

    private void connectToPeer(PeerInfo peer) throws IOException, InterruptedException {
        TcpClientSocket clientSocket = new TcpClientSocket(peer, peer.getTcpChatPort(), inputBox, this);
        Thread tcpClientThread = new Thread(clientSocket);
        tcpClientThread.start(); //enables us to recieve messages;
        Thread.sleep(3000);
        this.peerOutStream = clientSocket.getSocketOutStream();
        logging.append("peer out stream available\n");
//        tcpClientThread.join();//when chatting with peer we don't do anything else on the main thread ;
//        //main loop execution continues;
    }


//    //need to select the peer properly and how would i initiate the chat now?

    //how do i select the peer both the listener and ui has the selectedPeer
    //you parse the command you see the user wants to select (syntax :>  /select:num)
    //choose the peer from the map and also update for listener and initiate the chat;


    private void parseMessage(String input) {
        //now here we can have the complete startInputLoop code with some changes;
        if (input.startsWith("/select")) {
            //just get the substr after ':'
            int idx = input.indexOf(':');
            int peerNum = -1;
            if (idx != -1) {
                String result = input.substring(idx + 1);
                peerNum = Integer.parseInt(result);
            }
            if (peerNum <= peerList.size() && peerNum >= 1) {
                this.selectedPeer = peerList.get(peerNum);
                updateStatus("peer selected " + peerList.get(peerNum).getTcpChatPort());
                updateMessage("peer selected " + peerList.get(peerNum).getTcpChatPort());
            } else {
                updateStatus("peer doesn't exist! " + peerNum);
            }
        } else if (input.startsWith("/chat")) {
//            System.out.println("chatting with " + selectedPeer.toString());
            try {
                if (selectedPeer == null) {
                    updateStatus("no peer selected");
                } else {
                    connectToPeer(selectedPeer);//now how to setup the chat?
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            logging.append("chat message\n");
        }
//        else if (input.startsWith("/quit")) {
//            System.out.println("Exiting...");
//            System.exit(0);
//        } else {
//            System.out.println("invalid");
//        }

    }


}