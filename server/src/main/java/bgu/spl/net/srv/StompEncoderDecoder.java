package bgu.spl.net.srv;

import bgu.spl.net.srv.Messages.Message;
import bgu.spl.net.srv.Messages.ClientToServer.*;
import bgu.spl.net.srv.Messages.ServerToClient.*;
import bgu.spl.net.api.*;
import java.util.HashMap;
import java.util.Map;

public class StompEncoderDecoder implements MessageEncoderDecoder<Message> {

    private StringBuilder currentMessage = new StringBuilder();

    @Override
    public Message decodeNextByte(byte nextByte) 
    {
        if (nextByte == '\u0000') 
        {
            String message = currentMessage.toString();
            currentMessage.setLength(0);
            //System.out.println("incoming message: " + message);
            return parseClientToServerMessage(message);
        } 
        else 
        {
            currentMessage.append((char) nextByte);
            return null;
        }
    }

    @Override
    public byte[] encode(Message message) 
    {
        if (message instanceof ConnectedMessage) 
        {
            return encodeConnectedMessage((ConnectedMessage) message);
        } 
        else if (message instanceof ErrorMessage) 
        {
            return encodeErrorMessage((ErrorMessage) message);
        } 
        else if (message instanceof ReceiptMessage) 
        {
            return encodeReceiptMessage((ReceiptMessage) message);
        } 
        else if (message instanceof RegularMessage) 
        {
            return encodeRegularMessage((RegularMessage) message);
        } 
        else 
        {
            //TODO change to it to sent Error message
            throw new IllegalArgumentException("Unsupported message type for encoding: " + message.getClass().getName());
        }
    }

    private ClientToServerMessage parseClientToServerMessage(String message) 
    {
        String[] lines = message.split("\n");
    

        int startIdx = 0;
        while (startIdx < lines.length && lines[startIdx].isEmpty()) 
        {
            startIdx++;
        }
    
        if (startIdx == lines.length) 
        {
            return null;
        }
    
        String command = lines[startIdx];
        Map<String, String> headers = new HashMap<>();
        StringBuilder body = new StringBuilder();
        boolean inBody = false;
    
        for (int i = startIdx + 1; i < lines.length; i++) 
        {
            if (lines[i].isEmpty() && !inBody) 
            {
                inBody = true;
                continue;
            }
    
            if (inBody) 
            {
                body.append(lines[i]).append("\n");
            } 
            else 
            {
                String[] header = lines[i].split(":", 2);
                if (header.length == 2) 
                {
                    headers.put(header[0], header[1]);
                }
            }
        }
    
        ClientToServerMessage Msg;
        switch (command)
        {
            case "CONNECT":
                Msg = new ConnectMessage(headers.get("login"), headers.get("passcode"));
                break;
            case "DISCONNECT":
                Msg = new DisconnectMessage(Integer.parseInt(headers.get("receipt")));
                break;
            case "SEND":
                Msg = new SendMessage(headers.get("destination"), body.toString().trim());
                break;
            case "SUBSCRIBE":
                Msg = new SubscribeMessage(Integer.parseInt(headers.get("id")), headers.get("destination"));
                break;
            case "UNSUBSCRIBE":
                Msg = new UnsubscribeMessage(Integer.parseInt(headers.get("id")));
                break;
            default:
                return null;
        }
        return Msg;
    }
    

    private byte[] encodeConnectedMessage(ConnectedMessage message) 
    {
        return buildFrame("CONNECTED", new HashMap<>(), "version:1.2");
    }

    private byte[] encodeErrorMessage(ErrorMessage message) 
    {
        Map<String, String> headers = new HashMap<>();
        headers.put("message", message.getErrorDescription());
        return buildFrame("ERROR", headers, "");
    }

    private byte[] encodeReceiptMessage(ReceiptMessage message) 
    {
        Map<String, String> headers = new HashMap<>();
        headers.put("receipt-id", "" + message.getReceiptId());
        return buildFrame("RECEIPT", headers, "");
    }

    private byte[] encodeRegularMessage(RegularMessage message) 
    {
        Map<String, String> headers = new HashMap<>();
        headers.put("destination", message.getDestination());
        headers.put("subscription", String.valueOf(message.getSubscriptionId()));
        headers.put("message-id", String.valueOf(message.getMessageId()));
        return buildFrame("MESSAGE", headers, message.getBody());
    }
    private byte[] buildFrame(String command, Map<String, String> headers, String body) 
    {
        StringBuilder frame = new StringBuilder(command).append("\n");

        for (Map.Entry<String, String> entry : headers.entrySet()) 
        {
            frame.append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
        }
        frame.append("\n");
        if (!body.isEmpty()) 
        {
            frame.append(body).append("\n");
        }
        frame.append('\u0000').append("\n");
        return frame.toString().getBytes();
    }
}
