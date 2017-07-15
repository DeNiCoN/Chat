package sample;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.EndPoint;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import sample.net.ChatServer;
import sample.net.Packets;
import sample.net.User;

import java.io.IOException;

public class Main extends Application {

    public static ChatServer chatServer;
    public static User user;
    public TabPane root;
    private static Main instance;
    public Controller controller;
    public static boolean running;

    @Override
    public void start(Stage primaryStage) throws Exception{
        instance = this;
        running = true;
        root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        controller = new Controller();
    }

    public static Main getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                chatServer = new ChatServer(Integer.parseInt(args[0]));
            } catch (IOException e) {
                e.printStackTrace();
            }

            instance = new Main();
        } else {
            launch(args);
        }
    }

    static public void register (EndPoint endPoint) {
        Kryo kryo = endPoint.getKryo();
        kryo.register(Packets.ChatMessage.class);
        kryo.register(Packets.Login.class);
        kryo.register(Packets.ConsoleMessage.class);
        kryo.register(Packets.UpdateNames.class);
        kryo.register(String[].class);
        kryo.register(sample.net.Packets.Kick.class);
    }

    @Override
    public void stop() throws Exception {
        running = false;
        exit("");
        super.stop();
    }
    public void exit(String reason) {
        ((TextArea) Main.getInstance().root.getScene().lookup("#console")).setText("");
        ((TextArea) Main.getInstance().root.getScene().lookup("#chat")).setText("");
        root.getSelectionModel().getSelectedItem().setDisable(true);
        root.getSelectionModel().select(root.getSelectionModel().getSelectedIndex() - 1);
        root.getSelectionModel().getSelectedItem().setDisable(false);
        Platform.runLater(new Runnable(){
            @Override
            public void run() {
                Label exceptionLabel = ((Label) Main.getInstance().root.getScene().lookup("#exceptionLabel"));
                exceptionLabel.setText(reason);
            }
        });
        if (user != null && user.client != null) {
            user.client.stop();
        }
        if (chatServer != null && chatServer.server != null) {
            Packets.Kick kick = new Packets.Kick();
            kick.reason = "Host closed server";
            chatServer.server.sendToAllTCP(kick);
            chatServer.server.stop();
        }
    }
}
