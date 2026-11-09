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
 * Opens many connections to the benchmark server at the same time, sends one request on each, and
 * prints how many requests per second the server handled.
 *
 * <p>Usage: {@code java BenchmarkClient.java <SLEEP|COMPUTE> [connections]}, 1000 connections by
 * default.
 */
public class BenchmarkClient {

  private static final String HOST = "localhost";
  private static final int PORT = 6441;

  public static void main(String[] args) {
    String request = args.length > 0 ? args[0] : "SLEEP";
    int connections = args.length > 1 ? Integer.parseInt(args[1]) : 1000;
    AtomicInteger errors = new AtomicInteger();

    long start = System.nanoTime();
    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      for (int i = 0; i < connections; i++) {
        executor.submit(
            () -> {
              try (Socket socket = new Socket(HOST, PORT);
                  BufferedReader in =
                      new BufferedReader(
                          new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                  BufferedWriter out =
                      new BufferedWriter(
                          new OutputStreamWriter(
                              socket.getOutputStream(), StandardCharsets.UTF_8))) {
                out.write(request + "\n");
                out.flush();
                if (!"DONE".equals(in.readLine())) {
                  errors.incrementAndGet();
                }
              } catch (IOException e) {
                errors.incrementAndGet();
              }
            });
      }
    }
    double seconds = (System.nanoTime() - start) / 1e9;

    System.out.printf(
        "%d %s requests in %.2f s: %.0f requests per second, %d errors%n",
        connections, request, seconds, connections / seconds, errors.get());
  }
}
