import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An echo server: sends back every line it receives, until the client closes the connection.
 *
 * <p>Usage: {@code java EchoServer.java [virtual|single]}: one virtual thread per client (the
 * default), or a single thread for all the clients, one after the other. Port 6440.
 */
public class EchoServer {

  private static final int PORT = 6440;

  public static void main(String[] args) {
    boolean single = args.length > 0 && args[0].equals("single");

    try (ServerSocket serverSocket = new ServerSocket(PORT);
        ExecutorService executor =
            single
                ? Executors.newSingleThreadExecutor()
                : Executors.newVirtualThreadPerTaskExecutor()) {
      System.out.println(
          "[Server] Listening on port " + PORT + (single ? ", single thread" : ", virtual threads"));

      while (!serverSocket.isClosed()) {
        Socket socket = serverSocket.accept();
        executor.submit(() -> echo(socket));
      }
    } catch (IOException e) {
      System.out.println("[Server] Error: " + e.getMessage());
    }
  }

  private static void echo(Socket socket) {
    try (socket;
        BufferedReader in =
            new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        BufferedWriter out =
            new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = in.readLine()) != null) {
        out.write(line + "\n");
        out.flush();
      }
    } catch (IOException e) {
      System.out.println("[Server] Error with a client: " + e.getMessage());
    }
  }
}
