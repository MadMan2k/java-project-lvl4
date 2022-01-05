package hexlet.code.controllers;

import io.javalin.http.Context;

public class MainController {

//    public static Handler getWelcome() {
//        return welcome;
//    }

    public static void getWelcome(Context ctx) {
        ctx.render("index.html");
    }

//    public final static Handler welcome = ctx -> {
//        ctx.render("index.html");
//    };
}
