import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

/**
 * The receiving part of the "Temperature monitoring" server: joins the multicast group of the
 * sensors and keeps the latest temperature of each room. Answering the operator comes with
 * chapter 09: the server then listens on two sockets at the same time.
 *
 * <p>Usage: {@code java TemperatureReceiver.java [network interface]}. Without an interface, the
 * system chooses one. If nothing arrives, give the interface connected to the network of the
 * emitters ({@code en0} on a Mac, {@code eth0} on Linux and WSL).
 */
public class TemperatureReceiver {

  private static final String GROUP = "239.255.1.2";
  private static final int PORT = 6436;
  private static final int MAX_SIZE = 256;

  public static void main(String[] args) {
    // The latest temperature of each room, sorted by room name
    Map<String, String> temperatures = new TreeMap<>();

    // A MulticastSocket is a DatagramSocket that can join groups. It must be bound to the port
    // the emitters send to.
    try (MulticastSocket socket = new MulticastSocket(PORT)) {
      NetworkInterface networkInterface =
          args.length > 0 ? NetworkInterface.getByName(args[0]) : null;
      if (args.length > 0 && networkInterface == null) {
        System.out.println("[Receiver] Unknown network interface: " + args[0]);
        return;
      }
      InetSocketAddress group = new InetSocketAddress(GROUP, PORT);

      // From now on, the datagrams sent to the group reach this socket
      socket.joinGroup(group, networkInterface);
      System.out.println(
          "[Receiver] Joined " + GROUP + " on port " + PORT
              + (networkInterface != null ? " via " + networkInterface.getName() : ""));

      while (true) {
        byte[] buffer = new byte[MAX_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);

        String message =
            new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);

        // TEMP <room> <temperature>\n, single spaces; anything else is ignored, as the
        // specification says
        String[] parts =
            message.endsWith("\n")
                ? message.substring(0, message.length() - 1).split(" ", -1)
                : new String[0];
        if (parts.length == 3
            && parts[0].equals("TEMP")
            && parts[1].matches("[a-zA-Z0-9]{1,20}")
            && parts[2].matches("-?[0-9]+(\\.[0-9]+)?")) {
          temperatures.put(parts[1], parts[2]);
          System.out.println("[Receiver] From " + packet.getSocketAddress() + ": " + temperatures);
        } else {
          System.out.println("[Receiver] Ignored from " + packet.getSocketAddress() + ": " + message.strip());
        }
      }
    } catch (IOException e) {
      System.out.println("[Receiver] Error: " + e.getMessage());
    }
  }
}
