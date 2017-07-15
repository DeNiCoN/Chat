package sample.net;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import sample.Main;

import java.io.IOException;

public class User {
    public Client client;
    public final String name;

    public User(String ipAddress, int serverPort, boolean host, final String name) {
        this.name = name;
        client = new Client();
        client.start();

        // For consistency, the classes to be sent over the network are
        // registered by the same method for both the client and server.
        Main.register(client);

        client.addListener(new Listener() {
            public void connected (Connection connection) {
                Packets.Login login = new Packets.Login();
                login.name = name;
                client.sendTCP(login);
            }

            public void received (Connection connection, Object object) {
                System.out.println(object.toString());
                if (object instanceof Packets.ChatMessage) {
                    Packets.ChatMessage chatMessage = (Packets.ChatMessage)object;
                    TextArea chat = ((TextArea) Main.getInstance().root.getScene().lookup("#chat"));
                    String message = chatMessage.message;
                    String previous = chat.getText();
                    int caretPos = chat.getCaretPosition() + (message.length() - previous.length());
                    chat.setText(message);
                    chat.positionCaret(caretPos);
                }
                if (object instanceof Packets.ConsoleMessage) {
                    Packets.ConsoleMessage consoleMessage = (Packets.ConsoleMessage) object;
                    TextArea console = ((TextArea) Main.getInstance().root.getScene().lookup("#console"));
                    console.setText(console.getText() + consoleMessage.message);
                }
                if (object instanceof Packets.Kick) {
                    Packets.Kick kick = (Packets.Kick) object;
                    Main.getInstance().exit(kick.reason);
                }
                if (object instanceof Packets.UpdateNames) {
                    Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            ListView<String> usersList = ((ListView<String>) Main.getInstance().root.getScene().lookup("#usersList"));
                            ObservableList<String> list = FXCollections.observableArrayList();
                            list.setAll(((Packets.UpdateNames)object).names);
                            usersList.setItems(list);
                            usersList.refresh();
                        }
                    });
                }
            }
        });
        try {
            client.connect(5000, ipAddress, serverPort);
        } catch (IOException ex) {
            Main.getInstance().exit("Can't connect to the server");
        } catch (IllegalArgumentException e) {
            Main.getInstance().exit("port out of range, max: 65535");
        }
        if (host) {
            TextArea console = ((TextArea) Main.getInstance().root.getScene().lookup("#console"));
            console.setText(console.getText() + "Do not forget to open the port\n");
        }
    }
}
