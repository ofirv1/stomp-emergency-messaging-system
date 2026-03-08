package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public abstract class BaseServer<T> implements Server<T> {

    private final int port;
    private final Supplier<MessagingProtocol<T>> protocolFactory;
    private final Supplier<MessageEncoderDecoder<T>> encdecFactory;
    private final ConnectionsImpl<T> connections;
    private ServerSocket sock;
    private static final AtomicInteger connectionIdCounter = new AtomicInteger(0);

    public BaseServer(
            int port,
            Supplier<MessagingProtocol<T>> protocolFactory,
            Supplier<MessageEncoderDecoder<T>> encdecFactory) {

        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
        this.connections = ConnectionsImpl.getInstance();
        this.sock = null;
    }

    @Override
    public void serve() {

        try (ServerSocket serverSock = new ServerSocket(port)) {
            System.out.println("Server started");

            this.sock = serverSock; // just to be able to close

            while (!Thread.currentThread().isInterrupted()) {

                Socket clientSock = serverSock.accept();
                // MessageEncoderDecoder<T> encdec = encdecFactory.get();
                // T initialMessage = readInitialMessage(clientSock, encdec);
                int connectionId = connectionIdCounter.incrementAndGet(); ///
                MessagingProtocol<T> protocol = protocolFactory.get();
                protocol.start(connectionId, ConnectionsImpl.getInstance()); ///

                BlockingConnectionHandler<T> handler = new BlockingConnectionHandler<>(
                        clientSock,
                        encdecFactory.get(),
                        protocol);
                        
                connections.addConnection(connectionId, handler);
                execute(handler);
            }
        }
        catch (IOException ex) {}

        System.out.println("server closed!!!");
    }

    @Override
    public void close() throws IOException {
        if (sock != null)
            sock.close();
    }

    protected void execute(BlockingConnectionHandler<T> handler) {
        new Thread(handler).start();
    }
}
