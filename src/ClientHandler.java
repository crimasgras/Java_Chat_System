import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final Set<ClientHandler> clients;
    private boolean authenticated = false;
    private String user = "";

    public ClientHandler(Socket socket, Set<ClientHandler> clients) {
        this.socket = socket;
        this.clients = clients;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            out.println("USERNAME:");
            user = in.readLine();

            out.println("PASSWORD:");
            String pass = in.readLine();

            if (!Server.registerOrLogin(user, pass)) {
                out.println("LOGIN_FAILED");
                closeAll();
                return;
            }

            out.println("LOGIN_SUCCESS");
            authenticated = true;

        } catch (IOException e) {
            closeAll();
        }
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getUsername() {
        return user;
    }

    @Override
    public void run() {
        if (!authenticated) {
            closeAll();
            return;
        }

        broadcast("[System] " + user + " joined the chat!");
        out.println("[System] Welcome, " + user + "! There are " + clients.size() + " users online.");
        out.println("[System] Commands: /list, /history, /clear, /exit, /help");

        try {
            String msg;
            while ((msg = in.readLine()) != null) {
                if (msg.contains("/list")) {
                    StringBuilder list = new StringBuilder("[System] Online users: ");
                    synchronized (clients) {
                        for (ClientHandler c : clients) {
                            list.append(c.getUsername()).append(" ");
                        }
                    }
                    out.println(list);
                } else if (msg.contains("/history")) {
                    sendHistory();
                } else {
                    Server.logMessage(msg);
                    broadcast(msg);
                }
            }
        } catch (IOException e) {
            System.out.println("Connection lost: " + user);
        } finally {
            broadcast("[System] " + user + " left the chat.");
            closeAll();
        }
    }

    private void sendHistory() {
        try (BufferedReader reader = new BufferedReader(new FileReader("chat_log.txt"))) {
            out.println("[System] --- Chat history ---");
            String line;
            while ((line = reader.readLine()) != null) {
                out.println(line);
            }
            out.println("[System] --- End of history ---");
        } catch (IOException e) {
            out.println("[System] No history available.");
        }
    }

    private void broadcast(String msg) {
        synchronized (clients) {
            for (ClientHandler c : clients) {
                c.out.println(msg);
            }
        }
    }

    private void closeAll() {
        try {
            clients.remove(this);
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }
}
