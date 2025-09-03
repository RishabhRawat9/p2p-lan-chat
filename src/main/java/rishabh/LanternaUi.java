package rishabh;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.Theme;
import com.googlecode.lanterna.graphics.ThemeDefinition;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import java.io.IOException;
import java.util.List;

public class LanternaUi {

    public void createLayout() throws IOException {
        // Create terminal and screen
        Terminal terminal = new DefaultTerminalFactory().createTerminal();
        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();

        // Create GUI
        MultiWindowTextGUI gui = new MultiWindowTextGUI(screen);

        // Create main window
        BasicWindow window = new BasicWindow();
        window.setHints(List.of(Window.Hint.FULL_SCREEN, Window.Hint.NO_DECORATIONS));

        // Main panel with border layout
        Panel mainPanel = new Panel(new BorderLayout());
 // Grey color
        // Header section
        Panel headerPanel = new Panel(new BorderLayout());

        headerPanel.withBorder(Borders.singleLine("Header"));

        Panel headerContent = new Panel(new LinearLayout(Direction.HORIZONTAL));
        headerContent.addComponent(new Label("[Status]"));
        headerContent.addComponent(new Label("    Local Node ID: <your_id>"));
        headerContent.addComponent(new Label("   Connected Peers: N"));

        headerPanel.addComponent(headerContent, BorderLayout.Location.CENTER);

        // Chat window section
        Panel chatWindowPanel = new Panel(new BorderLayout());
        chatWindowPanel.withBorder(Borders.singleLine("Chat Window"));

        Panel chatContent = new Panel(new LinearLayout(Direction.VERTICAL));

        //ok so in this panel we would be making all the chat changes so this part would keep changing constantly, right/
        chatContent.addComponent(new Label("[Peer1] Hello!"));
        chatContent.addComponent(new Label("[Peer2] Hey, what's up?"));
        chatContent.addComponent(new Label("..."));

        chatWindowPanel.addComponent(chatContent, BorderLayout.Location.CENTER);

        // Input area section
        Panel inputPanel = new Panel(new BorderLayout());
        inputPanel.withBorder(Borders.singleLine("Input Area"));

        Panel inputContent = new Panel(new LinearLayout(Direction.HORIZONTAL));
        inputContent.addComponent(new Label("> "));

        TextBox messageInput = new TextBox(new TerminalSize(40, 1));
        messageInput.setText("Type message here...");
        inputContent.addComponent(messageInput);

        Panel buttonPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        buttonPanel.addComponent(new Button("Send"));
        buttonPanel.addComponent(new Label("[Enter to send]"));

        inputPanel.addComponent(inputContent, BorderLayout.Location.CENTER);
        inputPanel.addComponent(buttonPanel, BorderLayout.Location.RIGHT);

        // Assemble the layout
        mainPanel.addComponent(headerPanel, BorderLayout.Location.TOP);
        mainPanel.addComponent(chatWindowPanel, BorderLayout.Location.CENTER);
        mainPanel.addComponent(inputPanel, BorderLayout.Location.BOTTOM);

        window.setComponent(mainPanel);

        // Display the window
        gui.addWindowAndWait(window);
        screen.stopScreen();
    }


}