# TCP and UDP programming

Code examples for [chapter 08, TCP & UDP
programming](https://heigvd-dai-26.github.io/chapters/08-tcp-and-udp.html).
Try each example and understand how it works. They are ordered by
complexity; explore them in this order.

## Part 1/2

1. [TCP binary client/server](01-tcp-binary-client-server)
2. [TCP textual client/server](02-tcp-textual-client-server)

## Part 2/2

1. [UDP unicast client/server](03-udp-unicast-client-server)
2. [UDP broadcast emitter/receiver](04-udp-broadcast-emitter-receiver)
3. [UDP multicast emitter/receiver](05-udp-multicast-emitter-receiver)
4. [UDP request-response messaging pattern](06-udp-request-response-messaging-pattern)
5. [UDP fire-and-forget messaging pattern](07-udp-fire-and-forget-messaging-pattern)
6. [TCP REPL client/server](08-tcp-repl-client-server)

## Usage

Each program is a single Java file. Run it from its directory, in its own
terminal, with the server or receiver first:

```sh
java <ExampleName>.java
```

## Other directories

- [`exercises`](exercises): a broken textual client and server, to repair
  with ncat.
- [`templates`](templates): Maven projects with picocli, to start your own
  TCP or UDP application.
- [`solutions`](solutions): the *Guess the number* game (server and client),
  and the emitter and receiver of *Temperature monitoring*, following the
  specifications of chapter 06. The operator needs the receiver to read two
  sockets at the same time: chapter 09 explains how.
