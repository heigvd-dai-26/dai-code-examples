# UDP broadcast emitter/receiver example

An example of a UDP broadcast emitter/receiver.

The emitter sends to `255.255.255.255`, the whole local network. To use the
broadcast address of your subnet instead, change `BROADCAST_ADDRESS` in the code.
`ip address` (Linux, WSL) or `ifconfig` (macOS) shows it for each interface.

> [!IMPORTANT]
>
> If you are multiple people testing this code on the same network, you might
> receive messages from other people. To avoid this, you can change the port
> number in the code to a different one.
>
> This is one of the weaknesses of UDP broadcast. It is not possible to have a
> private broadcast on a network. All the devices on the same network will
> receive the broadcast messages.

Try to start the emitter before the receiver. What can you notice?

Then, restart the emitter after the receiver. What can you notice?
