package sample.net;


public class Packets {
    public static class Login {
        public String name;
    }
    public static class ConsoleMessage {
        public String message;
    }
    public static class ChatMessage {
        public String message;
    }
    public static class UpdateNames {
        public String[] names;
    }
    public static class Kick {
        public String reason;
    }
}
