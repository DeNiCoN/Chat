package sample;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import sample.net.ChatServer;
import sample.net.Packets;
import sample.net.User;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Controller {
    @FXML
    Button host;
    @FXML
    Button join;
    @FXML
    Button servers;
    @FXML
    TextField ip;
    @FXML
    TextField port;
    @FXML
    TextField portHost;
    @FXML
    private TextArea chat;
    @FXML
    private TextArea console;
    @FXML
    Tab chatTab;
    @FXML
    Tab startTab;
    @FXML
    TextField name;
    @FXML
    Label exceptionLabel;
    @FXML
    ListView<String> usersList;


    @FXML
    public void initialize() {

    }

    @FXML
    public void onJoin() {
        try {
            if (name.getText().length() > 0) {
                chatTab.setDisable(false);
                startTab.setDisable(true);
                Main.getInstance().root.getSelectionModel().select(chatTab);
                Main.user = new User(ip.getText(), Integer.parseInt(port.getText()), false, name.getText());
            } else {
                exceptionLabel.setText("Enter name before connection to server");
            }
        } catch (NumberFormatException e) {
            exceptionLabel.setText("Port is number");
        }
    }
    @FXML
    public void onHost() {
        try {
            if (name.getText().length() > 0) {
                chatTab.setDisable(false);
                startTab.setDisable(true);
                Main.getInstance().root.getSelectionModel().select(chatTab);
                Main.chatServer = new ChatServer(Integer.parseInt(portHost.getText()));
                Main.user = new User(InetAddress.getLocalHost().getHostAddress(), Integer.parseInt(portHost.getText()), true, name.getText());

            } else {
                exceptionLabel.setText("Enter name before connection to server");
            }
        } catch (NumberFormatException e) {
            exceptionLabel.setText("Port is number");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void onTextEdit() {
        Packets.ChatMessage chatMessage = new Packets.ChatMessage();
        chatMessage.message = chat.getText();
        Main.user.client.sendTCP(chatMessage);
    }

    @FXML
    public void onExit() {
        Main.getInstance().exit("");
    }
}
