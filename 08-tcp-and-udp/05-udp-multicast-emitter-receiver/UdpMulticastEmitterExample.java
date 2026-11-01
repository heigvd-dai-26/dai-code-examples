import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * This class implements a multicast emitter that sends messages to a multicast group.
 *
 * <p>A multicast emitter is a program that sends messages to a multicast group. It creates a
 * datagram socket and sends message to the group. The messages are sent to a multicast
 * address and a port. The messages are printed to the console.
 *
 * <p>Multicast addresses are in the range `224.0.0.0` to `239.255.255.255`. Use the range
 * `239.0.0.0` to `239.255.255.255` for your own applications. Choose a port above `1023`: lower
 * ports need special rights on some systems.
 */
public class UdpMulticastEmitterExample {
  // Multicast address on which the emitter sends messages
  private static final String MULTICAST_ADDRESS = "239.0.0.0";

  // Port on which the emitter sends messages
  private static final int PORT = 4446;

  // Message to send
  private static final String MESSAGE = "Hello, multicast listeners! 🎃";

  public static void main(String[] args) {
    System.out.println("[Emitter] Sending multicast messages...");

    // Create a datagram socket
    try (DatagramSocket socket = new DatagramSocket()) {
      // Get the multicast address
      InetAddress multicastAddress = InetAddress.getByName(MULTICAST_ADDRESS);

      // Transform the message into a byte array - always specify the encoding
      byte[] buffer = MESSAGE.getBytes(StandardCharsets.UTF_8);

      // Create a packet with the message, the multicast address and the port
      DatagramPacket packet = new DatagramPacket(buffer, buffer.length, multicastAddress, PORT);

      // Send the packet
      socket.send(packet);

      // Print the message
      System.out.println("[Emitter] Message sent: " + MESSAGE);
    } catch (Exception e) {
      System.err.println("[Emitter] An error occurred: " + e.getMessage());
    }
  }
}
