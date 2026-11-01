package ch.heigvd.dai.commands;

import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(
    name = "operator",
    description =
        "Start the operator part of the network application using the request-reply messaging pattern.")
public class Operator implements Callable<Integer> {

  @CommandLine.Option(
      names = {"-H", "--host"},
      description = "Host to connect to.",
      required = true)
  protected String host;

  @CommandLine.Option(
      names = {"-p", "--port"},
      description = "Port to use (default: ${DEFAULT-VALUE}).",
      defaultValue = "6437")
  protected int port;

  @Override
  public Integer call() {
    throw new UnsupportedOperationException(
        "Please remove this exception and implement this method.");
  }
}
