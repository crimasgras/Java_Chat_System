import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.io.*;
import java.net.*;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 1234;

    private String user;
    private BufferedReader in;
    private PrintWriter out;

    private final JFrame frame = new JFrame("Chat Client");
    private final JTextArea chatArea = new JTextArea(20, 40);
    private final JTextField inputField = new JTextField();
    private final JButton sendButton = new JButton("Send");

    public Client() {
        setupGUI();
        connect();
        if (user == null || user.trim().isEmpty()) {
            user = "Anonymous";
        }
        startListener();
    }

    private void setupGUI() {
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        JScrollPane scroll = new JScrollPane(chatArea);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        JPanel emojiPanel = new JPanel();
        for (String e : new String[]{"😊", "😂", "❤️", "👍", "😢", "🔥"}) {
            JButton btn = new JButton(e);
            btn.addActionListener(_ -> inputField.setText(inputField.getText() + e));
            emojiPanel.add(btn);
        }

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(inputPanel, BorderLayout.SOUTH);
        bottom.add(emojiPanel, BorderLayout.NORTH);

        frame.setLayout(new BorderLayout());
        frame.add(scroll, BorderLayout.CENTER);
        frame.add(bottom, BorderLayout.SOUTH);

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        sendButton.addActionListener(_ -> sendMessage());
        inputField.addActionListener(_ -> sendMessage());
    }

    private void connect() {
        try {
            Socket socket = new Socket(HOST, PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            if (!"USERNAME:".equals(in.readLine())) {
                showErrorAndExit("Invalid protocol (missing USERNAME)");
            }

            user = JOptionPane.showInputDialog(frame, "Username:");
            if (user == null) System.exit(0);
            out.println(user);

            if (!"PASSWORD:".equals(in.readLine())) {
                showErrorAndExit("Invalid protocol (missing PASSWORD)");
            }

            String pass = JOptionPane.showInputDialog(frame, "Password:");
            if (pass == null) System.exit(0);
            out.println(pass);

            if (!"LOGIN_SUCCESS".equals(in.readLine())) {
                showErrorAndExit("Login failed!");
            }

        } catch (IOException e) {
            showErrorAndExit("Connection error: " + e.getMessage());
        }
    }

    private void startListener() {
        Thread t = new Thread(() -> {
            String msg;
            try {
                while ((msg = in.readLine()) != null) {
                    final String line = msg;
                    SwingUtilities.invokeLater(() -> {
                        chatArea.append("[" + LocalTime.now()
                                .format(DateTimeFormatter.ofPattern("HH:mm")) + "] " + line + "\n");
                        Toolkit.getDefaultToolkit().beep();
                    });
                }
            } catch (IOException e) {
                showErrorAndExit("Connection lost.");
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void sendMessage() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty()) {
            if (msg.equalsIgnoreCase("/clear")) {
                chatArea.setText("");
            } else if (msg.equalsIgnoreCase("/exit")) {
                System.exit(0);
            } else if (msg.equalsIgnoreCase("/help")) {
                chatArea.append("[System] Commands: /list, /history, /clear, /exit, /help\n");
            } else if (msg.equalsIgnoreCase("/history")) {
                out.println(user + ": /history");
            } else {
                out.println(user + ": " + msg);
                String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
                chatArea.append("[" + time + "] Me: " + msg + "\n");
            }
            inputField.setText("");
        }
    }

    private void showErrorAndExit(String msg) {
        JOptionPane.showMessageDialog(frame, msg);
        System.exit(0);
    }

    static void main() {
        SwingUtilities.invokeLater(Client::new);
    }
}

