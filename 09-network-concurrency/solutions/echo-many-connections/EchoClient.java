import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Opens 100 connections to the echo server, as fast as possible. On each one, sends 1000 lines and
 * reads the answer to each line before sending the next one.
 *
 * <p>Usage: {@code java EchoClient.java [virtual|single]}: all the connections at the same time,
 * one virtual thread each (the default), or one connection after the other, on a single thread.
 */
public class EchoClient {

  private static final String HOST = "localhost";
  private static final int PORT = 6440;
  private static final int CONNECTIONS = 100;
  private static final int MESSAGES = 1000;

  private static final AtomicInteger errors = new AtomicInteger();

  public static void main(String[] args) {
    boolean single = args.length > 0 && args[0].equals("single");

    long start = System.nanoTime();
    // close(), at the end of the try, waits until all the connections are done
    try (ExecutorService executor =
        single ? Executors.newSingleThreadExecutor() : Executors.newVirtualThreadPerTaskExecutor()) {
      for (int i = 1; i <= CONNECTIONS; i++) {
        int id = i;
        executor.submit(() -> talk(id));
      }
    }
    double seconds = (System.nanoTime() - start) / 1e9;

    System.out.printf(
        "[Client] %d connections in %.2f s, %d errors%n", CONNECTIONS, seconds, errors.get());
  }

  private static void talk(int id) {
    try (Socket socket = new Socket(HOST, PORT);
        BufferedReader in =
            new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        BufferedWriter out =
            new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
      for (int i = 1; i <= MESSAGES; i++) {
        String message = "Connection " + id + ", message " + i;
        out.write(message + "\n");
        out.flush();
        if (!message.equals(in.readLine())) {
          System.out.println("[Client] Wrong answer on connection " + id);
          errors.incrementAndGet();
          return;
        }
      }
    } catch (IOException e) {
      System.out.println("[Client] Error on connection " + id + ": " + e.getMessage());
      errors.incrementAndGet();
    }
  }
}
