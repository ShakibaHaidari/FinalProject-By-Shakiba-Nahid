package websocket;
import service.MessageService;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class WebSocketServer {

    private final int port;
    private final MessageService messageService;
    private final List<WebsocketClientHandler> clients;
    private boolean running;

    public WebSocketServer(int port, MessageService messageService) {
        this.port = port;
        this.messageService = messageService;
        this.clients = new ArrayList<>();
        this.running = false;
    }

    public void start() {

        running = true;

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println(
                    "WebSocket server started: ws://localhost:" + port
            );

            while (running) {

                Socket clientSocket = serverSocket.accept();

                WebsocketClientHandler handler = new WebsocketClientHandler(clientSocket, messageService, this);
                addClient(handler);

                Thread thread = new Thread(handler);
                thread.start();
            }

        } catch (Exception e) {
            System.out.println(
                    "WebSocket server stopped: " + e.getMessage()
            );
        }
    }

    public synchronized void addClient(WebsocketClientHandler client) {
        clients.add(client);
    }

    public synchronized void removeClient(
            WebsocketClientHandler client
    ) {
        clients.remove(client);
    }

    public synchronized void broadcast(String message) {

        List<WebsocketClientHandler> disconnected =
                new ArrayList<>();

        for (WebsocketClientHandler client : clients) {

            try {
                client.sendMessage(message);

            } catch (Exception e) {
                disconnected.add(client);
            }
        }

        clients.removeAll(disconnected);
    }

    public void stop() {
        running = false;
    }
}