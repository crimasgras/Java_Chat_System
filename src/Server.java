import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Server {
    private static final int PORT = 1234;
    private static final Set<ClientHandler> clients =
            Collections.synchronizedSet(new HashSet<>());
    private static final String USER_FILE = "users.txt";
    private static final String CHAT_LOG = "chat_log.txt";
    private static int counter = 0;

    static void main() {
        System.out.println("Server started on port " + PORT);

        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = server.accept();

                if (clients.size() >= 10) {
                    PrintWriter tmp = new PrintWriter(socket.getOutputStream(), true);
                    tmp.println("Server is full! Try again later.");
                    socket.close();
                    continue;
                }

                ClientHandler handler = new ClientHandler(socket, clients);
                if (handler.isAuthenticated()) {
                    synchronized (clients) {
                        clients.add(handler);
                    }
                    new Thread(handler).start();
                }
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static synchronized boolean registerOrLogin(String user, String pass) {

        if (user == null || user.length() < 3 || user.contains(" ")) {
            return false;
        }

        try {
            File file = new File(USER_FILE);
            if (!file.exists()) file.createNewFile();

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(":");
                    if (parts.length >= 3 && parts[0].equals(user)) {
                        int salt = Integer.parseInt(parts[1]);
                        String saved = parts[2];
                        return saved.equals(hash(pass, salt));
                    }
                }
            }

            int salt = ++counter;
            String hashed = hash(pass, salt);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.write(user + ":" + salt + ":" + hashed);
                writer.newLine();
            }
            return true;

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    private static String hash(String pass, int salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest((pass + salt).getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available!", e);
        }
    }

    public static synchronized void logMessage(String msg) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CHAT_LOG, true))) {
            String time = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            writer.write("[" + time + "] " + msg);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}