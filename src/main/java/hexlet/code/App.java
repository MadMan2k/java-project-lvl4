package hexlet.code;

import io.javalin.Javalin;

public class App {
//    private static final int PORT = 7000;

    public static void main(String[] args) {
        System.getenv().getOrDefault("PORT", "4000");
        getApp().start();
    }

    private static Javalin getApp() {
        Javalin app = Javalin.create(config -> config.enableDevLogging());
        app.get("/", ctx -> ctx.result("Hello World"));
        return app;
    }
}
