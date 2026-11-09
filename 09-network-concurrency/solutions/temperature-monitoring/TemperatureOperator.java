import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * The operator of the "Temperature monitoring" protocol (chapter 06, code examples
 * 06-application-protocols/temperature-monitoring): asks the server for the latest temperature of
 * a room (request-response), and sends the request again if no answer arrives within 2 seconds.
 *
 * <p>Usage: {@code java TemperatureOperator.java <room> [host]}, localhost by default.
 */
public class TemperatureOperator {

  private static final int PORT = 6437;
  private static final int MAX_SIZE = 256;
  private static final int ATTEMPTS = 3;

  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Usage: java TemperatureOperator.java <room> [host]");
      return;
    }
    String room = args[0];
    String host = args.length > 1 ? args[1] : "localhost";

    // No port given: the system chooses one, and the server answers to it
    try (DatagramSocket socket = new DatagramSocket()) {
      socket.setSoTimeout(2000);
      byte[] request = ("REQ_TEMP " + room + "\n").getBytes(StandardCharsets.UTF_8);
      DatagramPacket requestPacket =
          new DatagramPacket(request, request.length, InetAddress.getByName(host), PORT);

      for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {
        socket.send(requestPacket);
        System.out.println("[Operator] Sent to " + host + ":" + PORT + ": REQ_TEMP " + room);

        DatagramPacket responsePacket = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
        try {
          socket.receive(responsePacket);
          String response =
              new String(
                  responsePacket.getData(),
                  responsePacket.getOffset(),
                  responsePacket.getLength(),
                  StandardCharsets.UTF_8);
          System.out.print("[Operator] Response: " + response);
          return;
        } catch (SocketTimeoutException e) {
          System.out.println("[Operator] No response within 2 seconds");
        }
      }
      System.out.println("[Operator] No response after " + ATTEMPTS + " attempts");
    } catch (IOException e) {
      System.out.println("[Operator] Error: " + e.getMessage());
    }
  }
}
