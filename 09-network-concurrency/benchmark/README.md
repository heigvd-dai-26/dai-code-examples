# Benchmark of the servers

A server that takes its strategy as an argument, and a client that opens 1,000
connections at the same time and sends one request on each. A request is
`SLEEP` (the server waits 100 ms, like a slow database) or `COMPUTE` (the server
computes for a few milliseconds). The client prints the number of requests per
second.

In one terminal, start the server with a strategy: `single`, `thread` (one
platform thread per client), `cached`, `fixed-<n>` (a fixed pool of `n`
threads, for example `fixed-4`) or `virtual`:

```sh
java BenchmarkServer.java virtual
```

In another one, start the client with the kind of request, and optionally the
number of connections:

```sh
java BenchmarkClient.java SLEEP
java BenchmarkClient.java COMPUTE 1000
```

Stop the server with `Ctrl` + `C` before starting another one: they all listen
on port 6441.
