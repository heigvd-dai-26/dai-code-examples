import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The server of the "Temperature monitoring" protocol (chapter 06, code examples
 * 06-application-protocols/temperature-monitoring): keeps the latest temperature of each room
 * sent by the emitters, and answers the operators.
 *
 * <p>It listens on two sockets at the same time, each one in its own thread: the multicast group
 * of the emitters (port 6436) and the requests of the operators (port 6437). Both threads use the
 * same map, so it must be a concurrent map.
 *
 * <p>Usage: {@code java TemperatureReceiver.java [network interface]}. Without an interface, the
 * system chooses one. If nothing arrives, give the interface connected to the network of the
 * emitters ({@code en0} on a Mac, {@code eth0} on Linux and WSL).
 */
public class TemperatureReceiver {

  private static final String GROUP = "239.255.1.2";
  private static final int EMITTERS_PORT = 6436;
  private static final int OPERATORS_PORT = 6437;
  private static final int MAX_SIZE = 256;
  private static final String ROOM = "[a-zA-Z0-9]{1,20}";
  private static final String TEMPERATURE = "-?[0-9]+(\\.[0-9]+)?";

  // The latest temperature of each room, written by the emitters thread, read by the operators
  // thread
  private static final ConcurrentMap<String, String> temperatures = new ConcurrentHashMap<>();

  public static void main(String[] args) {
    NetworkInterface networkInterface = null;
    if (args.length > 0) {
      try {
        networkInterface = NetworkInterface.getByName(args[0]);
      } catch (IOException e) {
        System.out.println("[Receiver] Error: " + e.getMessage());
      }
      if (networkInterface == null) {
        System.out.println("[Receiver] Unknown network interface: " + args[0]);
        return;
      }
    }
    NetworkInterface chosenInterface = networkInterface;

    // Two threads, one per socket. close() at the end of the try waits for both to end.
    try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
      executor.submit(() -> emittersWorker(chosenInterface));
      executor.submit(TemperatureReceiver::operatorsWorker);
    }
  }

  /** Receives the temperatures of the emitters (fire and forget). */
  private static void emittersWorker(NetworkInterface networkInterface) {
    try (MulticastSocket socket = new MulticastSocket(EMITTERS_PORT)) {
      socket.joinGroup(new InetSocketAddress(GROUP, EMITTERS_PORT), networkInterface);
      System.out.println("[Receiver] Joined " + GROUP + " on port " + EMITTERS_PORT);

      while (true) {
        DatagramPacket packet = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
        socket.receive(packet);
        String[] parts = parse(packet);

        // TEMP <room> <temperature>; anything else is ignored, as the specification says
        if (parts.length == 3
            && parts[0].equals("TEMP")
            && parts[1].matches(ROOM)
            && parts[2].matches(TEMPERATURE)) {
          temperatures.put(parts[1], parts[2]);
          System.out.println("[Receiver] From " + packet.getSocketAddress() + ": " + temperatures);
        } else {
          System.out.println("[Receiver] Ignored from " + packet.getSocketAddress());
        }
      }
    } catch (Exception e) {
      System.out.println("[Receiver] Emitters thread stopped: " + e);
    }
  }

  /** Answers the requests of the operators (request-response). */
  private static void operatorsWorker() {
    try (DatagramSocket socket = new DatagramSocket(OPERATORS_PORT)) {
      System.out.println("[Receiver] Waiting for operators on port " + OPERATORS_PORT);

      while (true) {
        DatagramPacket packet = new DatagramPacket(new byte[MAX_SIZE], MAX_SIZE);
        socket.receive(packet);
        String[] parts = parse(packet);

        String answer;
        if (parts.length == 2 && parts[0].equals("REQ_TEMP") && parts[1].matches(ROOM)) {
          String temperature = temperatures.get(parts[1]);
          answer = temperature != null ? "TEMP " + parts[1] + " " + temperature : "ERROR 1";
        } else {
          answer = "ERROR -1";
        }
        System.out.println("[Receiver] Answer to " + packet.getSocketAddress() + ": " + answer);

        // The answer goes back to the address and port the request came from
        byte[] payload = (answer + "\n").getBytes(StandardCharsets.UTF_8);
        socket.send(new DatagramPacket(payload, payload.length, packet.getSocketAddress()));
      }
    } catch (Exception e) {
      System.out.println("[Receiver] Operators thread stopped: " + e);
    }
  }

  /** Splits a message ending with a newline on single spaces; empty if there is no newline. */
  private static String[] parse(DatagramPacket packet) {
    String message =
        new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    if (!message.endsWith("\n")) {
      return new String[0];
    }
    return message.substring(0, message.length() - 1).split(" ", -1);
  }
}
