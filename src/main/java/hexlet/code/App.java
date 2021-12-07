package hexlet.code;

import io.javalin.Javalin;

public class App {
    public static void main(String[] args) {
        getApp().start(7000);
    }

    private static Javalin getApp() {
        Javalin app = Javalin.create(config -> config.enableDevLogging());
        app.get("/", ctx -> ctx.result("Hello World"));
        return app;
    }
}
