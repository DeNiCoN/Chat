package sample.net;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import sample.Main;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ChatServer {
    public Server server;

    public ChatServer(int port) throws IOException {
        server = new Server() {
            protected Connection newConnection () {
                return new ChatConnection();
            }
        };
        Main.register(server);
        server.addListener(new Listener() {
            public void received (Connection c, Object object) {
                ChatConnection connection = (ChatConnection)c;

                if (object instanceof Packets.Login) {
                    if (connection.name != null) return;
                    String name = ((Packets.Login)object).name;
                    if (name == null) return;
                    name = name.trim();
                    if (name.length() == 0) return;
                    connection.name = name;
                    Packets.ConsoleMessage consoleMessage1 = new Packets.ConsoleMessage();
                    try {
                        consoleMessage1.message = "Connected to [" + InetAddress.getLocalHost().getHostAddress() + ":" + port + "].";
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    server.sendToTCP(connection.getID(), consoleMessage1);
                    Packets.ConsoleMessage consoleMessage = new Packets.ConsoleMessage();
                    consoleMessage.message = "\n" + name + " connected.";
                    System.out.println(name + " connected.");
                    server.sendToAllExceptTCP(connection.getID(), consoleMessage);
                    updateNames();
                    return;
                }

                if (object instanceof Packets.ChatMessage) {
                    if (connection.name == null) return;
                    Packets.ChatMessage chatMessage = (Packets.ChatMessage)object;
                    server.sendToAllTCP(chatMessage);
                }
            }

            public void disconnected (Connection c) {
                ChatConnection connection = (ChatConnection)c;
                if (connection.name != null) {
                    Packets.ConsoleMessage chatMessage = new Packets.ConsoleMessage();
                    chatMessage.message = "\n" + connection.name + " disconnected.";
                    System.out.println(connection.name + " disconnected.");
                    server.sendToAllTCP(chatMessage);
                    updateNames();
                }
            }
        });
        try {
            server.bind(port);
        } catch (IllegalArgumentException e) {
            Main.getInstance().exit("port out of range, max: 65535");
        }
        server.start();
    }

    public void updateNames () {
        Connection[] connections = server.getConnections();
        ArrayList names = new ArrayList(connections.length);
        for (int i = connections.length - 1; i >= 0; i--) {
            ChatConnection connection = (ChatConnection)connections[i];
            names.add(connection.name);
        }
        Packets.UpdateNames updateNames = new Packets.UpdateNames();
        updateNames.names = (String[])names.toArray(new String[names.size()]);
        server.sendToAllTCP(updateNames);
    }

    public ChatConnection getConnectionByName(String name) {
        for (Connection connection : server.getConnections()
             ) {
            if (((ChatConnection) connection).name.equals(name)) {
                return (ChatConnection) connection;
            }
        }
        return null;
    }
    public static class ChatConnection extends Connection {
        public String name;
    }
}

