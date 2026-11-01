package ch.heigvd.dai.commands;

import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(
    name = "emitter",
    description =
        "Start the emitter part of the network application using the fire-and-forget messaging pattern.")
public class Emitter implements Callable<Integer> {

  @CommandLine.Option(
      names = {"-M", "--multicast-address"},
      description = "Multicast address to use (default: ${DEFAULT-VALUE}).",
      defaultValue = "239.255.1.2")
  protected String multicastAddress;

  @CommandLine.Option(
      names = {"-P", "--port"},
      description = "Port to use (default: ${DEFAULT-VALUE}).",
      defaultValue = "6436")
  protected int port;

  @CommandLine.Option(
      names = {"-F", "--frequency"},
      description =
          "Frequency of sending the message (in milliseconds) (default: ${DEFAULT-VALUE}).",
      defaultValue = "5000")
  protected int frequency;

  @Override
  public Integer call() {
    throw new UnsupportedOperationException(
        "Please remove this exception and implement this method.");
  }
}
