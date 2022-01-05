package hexlet.code.controllers;

import io.javalin.http.Context;

public class MainController {

    public static void getWelcome(Context ctx) {
        ctx.render("index.html");
    }
}
