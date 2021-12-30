package hexlet.code;

import hexlet.code.controllers.ArticleController;
import hexlet.code.controllers.MainController;
import io.javalin.Javalin;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;

public class App {

//    private static String port = System.getenv().getOrDefault("PORT", "4000");

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "4000");
        return Integer.parseInt(port);
    }

    private static String getMode() {
        return System.getenv().getOrDefault("APP_ENV", "development");
    }

    private static boolean isProduction() {
        return getMode().equals("production");
    }

    public static void main(String[] args) {
//        getApp().start(Integer.parseInt(port));
        getApp().start(getPort());
    }

    public static Javalin getApp() {
        Javalin app = Javalin.create(config -> {
            if (!isProduction()) {
                config.enableDevLogging();
            }
            config.enableWebjars();
            JavalinThymeleaf.configure(getTemplateEngine());
        });

        addRoutes(app);

        app.before(ctx -> ctx.attribute("ctx", ctx));

//        app.get("/", ctx -> ctx.render("index.html"));

        return app;
    }

    public static void addRoutes(Javalin app) {
        app.get("/", MainController.getWelcome());
//        app.get("/about", MainController.getAbout());

        app.routes(() -> {
            path("urls", () -> {
                get(ArticleController.getListArticles());
                post(ArticleController.getCreateArticle());
                // Handler not exist in my app
//                get("new", ArticleController.newArticle);
                path("{id}", () -> {
                    get(ArticleController.getShowArticle());
                });
            });
        });
    }

    private static TemplateEngine getTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");

        templateEngine.addTemplateResolver(templateResolver);
        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new Java8TimeDialect());

        return templateEngine;
    }
}
