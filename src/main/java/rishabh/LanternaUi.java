package rishabh;
import com.googlecode.lanterna.graphics.*;
import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.graphics.Theme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.TextBox.DefaultTextBoxRenderer;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.TerminalSize;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

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

    public void setSelectedPeer(PeerInfo selectedPeer) {
        this.selectedPeer = selectedPeer;
    }

    public TextBox getMessageArea() {
        return messageArea;
    }

    private volatile boolean running = true;
    Theme theme = new SimpleTheme(
            TextColor.ANSI.BLACK_BRIGHT,
            TextColor.ANSI.RED_BRIGHT
    );


    public void createLayout() throws IOException {
        Terminal terminal = new DefaultTerminalFactory().createTerminal();
        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();

        gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLACK));
        gui.setTheme(new SimpleTheme(TextColor.ANSI.GREEN, TextColor.ANSI.BLACK));

        window = new BasicWindow();
        window.setHints(Arrays.asList(Window.Hint.CENTERED, Window.Hint.FIT_TERMINAL_WINDOW));
        window.setTitle("P2P Messenger");

        Panel contentPanel = new Panel();
        contentPanel.setLayoutManager(new BorderLayout());

        // Status bar at top
        statusLabel = new Label("Status: Starting...")
                .setBackgroundColor(TextColor.ANSI.BLACK)
                .setForegroundColor(TextColor.ANSI.YELLOW);
        Panel statusBar = new Panel(new LinearLayout(Direction.HORIZONTAL));
        statusBar.addComponent(statusLabel);
        contentPanel.addComponent(statusBar, BorderLayout.Location.TOP);

        // Main content area
        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

        // Message area (left side)
        Panel messagePanel = new Panel();
        messagePanel.setLayoutManager(new BorderLayout());
        messageArea = new TextBox("", TextBox.Style.MULTI_LINE)
                .setReadOnly(true)
                .setPreferredSize(new TerminalSize(60, 20));
        messagePanel.addComponent(messageArea, BorderLayout.Location.CENTER);

        // Input area at bottom of messages
        Panel inputPanel = new Panel();
        inputPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
        inputBox = new TextBox().setPreferredSize(new TerminalSize(60, 3));

        inputBox.setTheme(theme);


        inputBox.setInputFilter((interactable, key) -> {
            if (key.getKeyType() == KeyType.Enter) {
                String message = inputBox.getText();
                logging.append("Enter pressed\n");
                parseMessage(message);
                if (!message.trim().isEmpty() && selectedPeer != null) {
                    sendMessage(message);
                } else {
                    updateStatus("select a peer first");
                }
                inputBox.setText("");
                logging.flush();
                return false;
            }
            return true;
        });


        inputPanel.addComponent(inputBox);
        messagePanel.addComponent(inputPanel, BorderLayout.Location.BOTTOM);
        mainPanel.addComponent(messagePanel);

        // Right panel with peers
        Panel rightPanel = new Panel();
        Label panelHeading = new Label("PEERS");
        rightPanel.setLayoutManager(new BorderLayout());
        peersPanel = new Panel();
        peersPanel.addComponent(panelHeading);
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
            gui.getGUIThread().invokeLater(() -> statusLabel.setText("Status: " + status));
        }
    }

    public void updateMessage(String message) {
        if (messageArea != null) {
            gui.getGUIThread().invokeLater(() -> {
                String currentText = messageArea.getText();
                messageArea.setText(currentText + (currentText.isEmpty() ? "" : "\n") + message);
                messageArea.setCaretPosition(messageArea.getLineCount()); // auto-scroll
            });
        }
    }

    public void updatePeers() {
        if (peersPanel != null) {
            gui.getGUIThread().invokeLater(() -> {
                peersPanel.removeAllComponents();
                peersPanel.addComponent(new Label("PEERS"));
                peerList.forEach((id, peer) -> {
                    Label peerLabel = new Label("[" + id + " " + peer.getPort() + "] ")
                            .setForegroundColor(TextColor.ANSI.CYAN);
                    peersPanel.addComponent(peerLabel);
                });
                peersPanel.invalidate();//tell the gui thread to redraw this component;
            });
        }
    }

    private void sendMessage(String message) {
        String displayMessage = message;
        if (peerOutStream != null) {
            peerOutStream.write(displayMessage + "\n");
            logging.append(displayMessage);
            peerOutStream.flush();
            updateMessage("[YOU]: " + displayMessage);
        } else {
            logging.append("no peer connected\n");
        }
    }

    private void connectToPeer(PeerInfo peer) throws IOException, InterruptedException {
        TcpClientSocket clientSocket = new TcpClientSocket(peer, peer.getTcpChatPort(), inputBox, this);
        Thread tcpClientThread = new Thread(clientSocket);
        tcpClientThread.start();
        Thread.sleep(3000);
        this.peerOutStream = clientSocket.getSocketOutStream();
        logging.append("peer out stream available\n");
    }

    private void parseMessage(String input) {
        if (input.startsWith("/select")) {
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
            try {
                if (selectedPeer == null) {
                    updateStatus("no peer selected");
                } else {
                    connectToPeer(selectedPeer);
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            logging.append("chat message\n");
        }
    }
}
