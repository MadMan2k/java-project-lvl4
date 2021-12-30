package hexlet.code.controllers;

import io.javalin.http.Handler;

public class MainController {

    public static Handler getWelcome() {
        return welcome;
    }

//    public static Handler getAbout() {
//        return about;
//    }

    private static Handler welcome = ctx -> {
        ctx.render("index.html");
    };

//    private static Handler about = ctx -> {
//        ctx.render("about.html");
//    };
}
