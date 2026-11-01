import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Random;

/**
 * A temperature sensor of the "Temperature monitoring" protocol (chapter 06, code examples
 * 06-application-protocols/temperature-monitoring): sends the temperature of its room to a
 * multicast group at regular intervals, and expects no answer (fire and forget).
 *
 * <p>Usage: {@code java TemperatureEmitter.java [room] [seconds]}, "Kitchen" every 5 seconds by
 * default. The specification says every 10 minutes; 5 seconds is better for a demo.
 */
public class TemperatureEmitter {

  private static final String GROUP = "239.255.1.2";
  private static final int PORT = 6436;

  public static void main(String[] args) throws InterruptedException {
    String room = args.length > 0 ? args[0] : "Kitchen";
    int seconds = args.length > 1 ? Integer.parseInt(args[1]) : 5;

    Random random = new Random();
    double temperature = 18 + random.nextInt(6);

    // Sending to a multicast group needs no special socket: the address does the work
    try (DatagramSocket socket = new DatagramSocket()) {
      InetAddress group = InetAddress.getByName(GROUP);

      while (true) {
        // Locale.ROOT: always a "." as decimal separator, as the specification requires
        String message = String.format(Locale.ROOT, "TEMP %s %.1f\n", room, temperature);
        byte[] payload = message.getBytes(StandardCharsets.UTF_8);
        socket.send(new DatagramPacket(payload, payload.length, group, PORT));
        System.out.print("[Emitter] Sent to " + GROUP + ":" + PORT + ": " + message);

        Thread.sleep(seconds * 1000L);
        temperature += random.nextInt(-5, 6) / 10.0;
      }
    } catch (IOException e) {
      System.out.println("[Emitter] Error: " + e.getMessage());
    }
  }
}
