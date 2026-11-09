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
 * A server that does some work for each request, with the strategy given on the command line.
 *
 * <p>Usage: {@code java BenchmarkServer.java <strategy>}, port 6441. Strategies: {@code single},
 * {@code thread} (one new platform thread per client), {@code cached}, {@code fixed-<n>} (a fixed
 * pool of n threads, e.g. {@code fixed-4}), {@code virtual}.
 *
 * <p>Each client sends one line: {@code SLEEP} (wait 100 ms, like a slow database) or {@code
 * COMPUTE} (a computation of a few milliseconds). The server answers {@code DONE}.
 */
public class BenchmarkServer {

  private static final int PORT = 6441;
  // Room for all the connections of the benchmark in the queue of the operating system
  private static final int BACKLOG = 2000;

  public static void main(String[] args) throws IOException {
    String strategy = args.length > 0 ? args[0] : "virtual";

    try (ServerSocket serverSocket = new ServerSocket(PORT, BACKLOG)) {
      System.out.println("[Server] Listening on port " + PORT + ", strategy " + strategy);

      if (strategy.equals("single")) {
        while (true) {
          handle(serverSocket.accept());
        }
      } else if (strategy.equals("thread")) {
        while (true) {
          Socket socket = serverSocket.accept();
          new Thread(() -> handle(socket)).start();
        }
      } else {
        try (ExecutorService executor = executor(strategy)) {
          while (true) {
            Socket socket = serverSocket.accept();
            executor.submit(() -> handle(socket));
          }
        }
      }
    }
  }

  private static ExecutorService executor(String strategy) {
    if (strategy.equals("cached")) {
      return Executors.newCachedThreadPool();
    }
    if (strategy.startsWith("fixed-")) {
      return Executors.newFixedThreadPool(Integer.parseInt(strategy.substring(6)));
    }
    if (strategy.equals("virtual")) {
      return Executors.newVirtualThreadPerTaskExecutor();
    }
    throw new IllegalArgumentException("Unknown strategy: " + strategy);
  }

  private static void handle(Socket socket) {
    try (socket;
        BufferedReader in =
            new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        BufferedWriter out =
            new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
      String request = in.readLine();
      if ("SLEEP".equals(request)) {
        Thread.sleep(100);
      } else if ("COMPUTE".equals(request)) {
        compute();
      }
      out.write("DONE\n");
      out.flush();
    } catch (IOException | InterruptedException e) {
      System.out.println("[Server] Error with a client: " + e.getMessage());
    }
  }

  /** Keeps a core busy for a few milliseconds: counts the prime numbers below 200,000. */
  static int compute() {
    int count = 0;
    for (int n = 2; n < 200_000; n++) {
      boolean prime = true;
      for (int d = 2; d * d <= n; d++) {
        if (n % d == 0) {
          prime = false;
          break;
        }
      }
      if (prime) {
        count++;
      }
    }
    return count;
  }
}
