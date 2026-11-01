# UDP fire-and-forget messaging pattern example

An example of a UDP fire-and-forget messaging pattern.

> [!WARNING]
>
> Without a valid network interface in the code, the system chooses one. If nothing
> arrives, set the network interface to the one you want to use.
>
> To do so, you can use the following command:
>
> ```sh
> # Get the list of network interfaces (Linux, WSL)
> ip address
>
> # Get the list of network interfaces (macOS)
> ifconfig
> ```
>
> Then, you can set the network interface to the one you want to use in the
> code.

Try to start one emitter and one receiver. What can you notice?

Then, start a new receiver. What can you notice?
