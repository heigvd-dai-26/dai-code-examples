# Network concurrency

Code examples for [chapter 09, Network
concurrency](https://heigvd-dai-26.github.io/chapters/09-network-concurrency.html):
TCP servers that handle their clients in different ways. Try each example and
understand how it works. They are ordered by complexity; explore them in this
order.

1. [`TcpClientExample`](01-tcp-client-example): the client of all the servers.
2. [`TcpServerSimpleExample`](02-tcp-server-simple-example)
3. [`TcpServerSingleThreadExample`](03-tcp-server-single-thread-example)
4. [`TcpServerMultiThreadExample`](04-tcp-server-multi-thread-example)
5. [`TcpServerCachedThreadPoolExample`](05-tcp-server-cached-thread-pool-example)
6. [`TcpServerFixedThreadPoolExample`](06-tcp-server-fixed-thread-pool-example)
7. [`TcpServerVirtualThreadExample`](07-tcp-server-virtual-thread-example)
8. [`TcpServerConcurrentDataStructuresExample`](08-tcp-server-with-concurrent-data-structures-example)

Each server takes 10 seconds to answer, to simulate a long operation. All of
them listen on port 1234: run one server at a time.

## Usage

Each program is a single Java file. Start a server from its directory, in its
own terminal:

```sh
java <ExampleName>.java
```

Then, from `01-tcp-client-example`, start three clients at the same time, and
watch when each one gets its answer:

```sh
for i in 1 2 3; do java TcpClientExample.java & done; wait
```

## Benchmark

[`benchmark`](benchmark) measures the requests per second of each strategy,
when the server waits and when it computes (*Go further* of the chapter).

## Solutions

[`solutions`](solutions) holds the solutions of the exercises of the chapter:

- `echo-many-connections`: exercise 3, an echo server and a client that opens
  100 connections. `single` as argument uses a single thread instead of
  virtual threads, on both sides.

And of the *Go further* exercises, following the specifications of chapter 06:

- `guess-the-number/GuessServer.java`: the server of chapter 08, with one
  thread per client. The client of chapter 08 works unchanged.
- `temperature-monitoring`: the receiver listens to the emitters and to the
  operators at the same time, in two threads; `TemperatureOperator.java` asks
  for the temperature of a room. The emitter of chapter 08 works unchanged.
