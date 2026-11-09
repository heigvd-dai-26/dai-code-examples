import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The server of the "Guess the number" protocol (chapter 06, code examples
 * 06-application-protocols/guess-the-number).
 *
 * <p>Usage: {@code java GuessServer.java [port]}, port 6433 by default.
 *
 * <p>The network code (main) only reads lines, passes them to a {@link Game}, and writes the
 * answers. The rules of the protocol are all in {@link Game}, which knows nothing about sockets.
 *
 * <p>Several clients at the same time: each connection is handled by its own virtual thread, with
 * its own {@link Game}. The threads share nothing, so no concurrent data structure is needed.
 */
public class GuessServer {

  public static void main(String[] args) {
    int port = args.length > 0 ? Integer.parseInt(args[0]) : 6433;

    try (ServerSocket serverSocket = new ServerSocket(port);
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      System.out.println("[Server] Listening on port " + port);

      while (!serverSocket.isClosed()) {
        Socket socket = serverSocket.accept();
        // The main thread goes straight back to accept(); the new thread serves this client
        executor.submit(new ClientHandler(socket));
      }
    } catch (IOException e) {
      System.out.println("[Server] Cannot listen on port " + port + ": " + e.getMessage());
    }
  }

  /** Serves one client, from its first message to the end of the connection. */
  static class ClientHandler implements Runnable {
    private final Socket socket;

    ClientHandler(Socket socket) {
      this.socket = socket;
    }

    @Override
    public void run() {
      String client = socket.getRemoteSocketAddress().toString();

      try (socket; // closes the socket at the end of the try, like the streams
          BufferedReader in =
              new BufferedReader(
                  new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
          BufferedWriter out =
              new BufferedWriter(
                  new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
        System.out.println("[Server] Client connected from " + client);
        Game game = new Game(new Random());

        String line;
        while ((line = in.readLine()) != null) {
          System.out.println("[Server] " + client + " sent: " + line);
          String answer = game.handle(line);
          if (answer == null) {
            // QUIT: no answer, the server closes the connection
            break;
          }
          System.out.println("[Server] Answer to " + client + ": " + answer);
          out.write(answer + "\n");
          out.flush();
        }

        System.out.println("[Server] Connection closed with " + client);
      } catch (IOException e) {
        System.out.println("[Server] Error with " + client + ": " + e.getMessage());
      }
    }
  }

  /** The state of one game and the rules of the protocol. One instance per connection. */
  static class Game {
    private final Random random;
    private int number;
    private boolean over;

    Game(Random random) {
      this.random = random;
      newNumber();
    }

    private void newNumber() {
      number = random.nextInt(1, 101);
      over = false;
      System.out.println("[Server] New game, the number is " + number);
    }

    /** Returns the answer to one message, or null when the connection must be closed (QUIT). */
    String handle(String line) {
      String[] parts = line.split(" ", -1);

      switch (parts[0]) {
        case "GUESS" -> {
          if (parts.length != 2) {
            return "ERROR -1";
          }
          return guess(parts[1]);
        }
        case "RESTART" -> {
          if (parts.length != 1) {
            return "ERROR -1";
          }
          if (!over) {
            return "ERROR 1";
          }
          newNumber();
          return "OK";
        }
        case "QUIT" -> {
          if (parts.length != 1) {
            return "ERROR -1";
          }
          return null;
        }
        default -> {
          return "ERROR -1";
        }
      }
    }

    private String guess(String text) {
      // An integer: an optional "-", then digits
      if (!text.matches("-?[0-9]+")) {
        return "ERROR 2";
      }
      // At most 3 digits after leading zeros, so that parseInt cannot overflow
      if (!text.matches("0*[0-9]{1,3}")) {
        return "ERROR 1";
      }
      int guess = Integer.parseInt(text);
      if (guess < 1 || guess > 100) {
        return "ERROR 1";
      }
      if (over) {
        return "ERROR 3";
      }
      if (number < guess) {
        return "LOWER";
      }
      if (number > guess) {
        return "HIGHER";
      }
      over = true;
      return "CORRECT";
    }
  }
}
