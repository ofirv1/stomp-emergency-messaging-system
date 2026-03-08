package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.Server;
import bgu.spl.net.srv.StompEncoderDecoder;
import bgu.spl.net.srv.StompMessagingProtocolImplementation;
import bgu.spl.net.srv.Messages.Message;

public class StompServer {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: StompServer <port> <server type>");
            System.out.println("Server type: tpc | reactor");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number.");
            return;
        }

        String serverType = args[1];

        if (serverType.equalsIgnoreCase("tpc")) {
            Server.<Message>threadPerClient(
                port,
                () -> new StompMessagingProtocolImplementation(),
                () -> new StompEncoderDecoder()
            ).serve();
            

        } else if (serverType.equalsIgnoreCase("reactor")) {
            Server.<Message>reactor(
                Runtime.getRuntime().availableProcessors(), // Number of threads
                port,
                () -> new StompMessagingProtocolImplementation(),
                () -> new StompEncoderDecoder()
            ).serve();
        } else {
            System.err.println("Invalid server type. Use 'tpc' or 'reactor'.");
        }
    }
}

