import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * A client of the "Guess the number" protocol, with a REPL (Read-Eval-Print Loop): the user types
 * commands, the client turns them into messages and prints the answers, until the user quits.
 *
 * <p>Usage: {@code java GuessClient.java [host] [port]}, localhost 6433 by default.
 */
public class GuessClient {

  public static void main(String[] args) {
    String host = args.length > 0 ? args[0] : "localhost";
    int port = args.length > 1 ? Integer.parseInt(args[1]) : 6433;

    try (Socket socket = new Socket(host, port);
        BufferedReader in =
            new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        BufferedWriter out =
            new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        BufferedReader keyboard =
            new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
      System.out.println("Connected to " + host + ":" + port + ". Guess a number between 1 and 100.");
      help();

      while (true) {
        // Read
        System.out.print("> ");
        String input = keyboard.readLine();
        if (input == null) {
          // End of the keyboard input (Ctrl+D): leave as with "quit"
          input = "quit";
        }
        input = input.trim();

        // Eval: turn the command into a message of the protocol
        String message;
        if (input.equals("quit")) {
          message = "QUIT";
        } else if (input.equals("restart")) {
          message = "RESTART";
        } else if (input.matches("-?[0-9]+")) {
          message = "GUESS " + input;
        } else {
          help();
          continue;
        }

        out.write(message + "\n");
        out.flush();

        if (message.equals("QUIT")) {
          // The server closes the connection without answering
          System.out.println("Bye.");
          return;
        }

        String answer = in.readLine();
        if (answer == null) {
          System.out.println("The server closed the connection.");
          return;
        }

        // Print
        System.out.println(describe(message, answer));
      }
    } catch (IOException e) {
      System.out.println("Network error: " + e.getMessage());
    }
  }

  private static void help() {
    System.out.println("Commands: <number>, restart, quit");
  }

  /**
   * Turns an answer of the server into a sentence for the user. The error codes are numbered per
   * message: "ERROR 1" means something else after GUESS and after RESTART.
   */
  private static String describe(String message, String answer) {
    if (answer.equals("ERROR -1")) {
      return "Error: the server did not understand the message.";
    }
    if (message.equals("RESTART")) {
      return switch (answer) {
        case "OK" -> "New game: guess a number between 1 and 100.";
        case "ERROR 1" -> "Error: the game is still running.";
        default -> "Unexpected answer from the server: " + answer;
      };
    }
    return switch (answer) {
      case "LOWER" -> "Lower.";
      case "HIGHER" -> "Higher.";
      case "CORRECT" -> "Correct! Type restart to play again, or quit.";
      case "ERROR 1" -> "Error: the number must be between 1 and 100.";
      case "ERROR 2" -> "Error: not an integer.";
      case "ERROR 3" -> "The game is over: type restart or quit.";
      default -> "Unexpected answer from the server: " + answer;
    };
  }
}
