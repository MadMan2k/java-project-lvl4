package hexlet.code.controllers;

import hexlet.code.model.UrlModel;
import hexlet.code.model.query.QUrlModel;
import hexlet.code.service.UrlStandardizer;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ArticleController {

    public static Handler listArticles = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;
        int rowsPerPage = 10;

        PagedList<UrlModel> pagedUrlModels = new QUrlModel()
                .setFirstRow(page * rowsPerPage)
                .setMaxRows(rowsPerPage)
                .orderBy()
                .id.asc()
                .findPagedList();

        List<UrlModel> urlModels = pagedUrlModels.getList();

        UrlModel urlModel1 = new QUrlModel().id.equalTo(1).query().findOne();

//        UrlModel urlModel1 = new QUrlModel().id.equalTo(1).findOne();
//        UrlModel urlModel2 = new QUrlModel().id.equalTo(2).findOne();
//        UrlModel urlModel3 = new QUrlModel().id.equalTo(3).findOne();

        System.out.println(urlModel1);
//        System.out.println(urlModel2);
//        System.out.println(urlModel3);

//        URL url1 = new URL(new QUrlModel().id.equalTo(1).findOne().getName());
//        URL url2 = new URL(new QUrlModel().id.equalTo(2).findOne().getName());
//        URL url3 = new URL(new QUrlModel().id.equalTo(3).findOne().getName());
//
//        System.out.println("Ето url1 " + url1);
//        System.out.println("Ето url2 " + url2);
//        System.out.println("Ето url3 " + url3);




        for (UrlModel urlModel : urlModels) {
            System.out.println(urlModel);
        }

        int lastPage = pagedUrlModels.getTotalPageCount() + 1;
        int currentPage = pagedUrlModels.getPageIndex() + 1;
        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .collect(Collectors.toList());

        ctx.attribute("urlModels", urlModels);
        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);
        ctx.render("articles/index.html");
    };

    // Handler not exist in my app
//    public static Handler newArticle = ctx -> {
//        UrlModel article = new UrlModel();
//
//        ctx.attribute("article", article);
//        ctx.render("articles/new.html");
//    };

    public static Handler createArticle = ctx -> {
        String inputURL = ctx.formParam("url");
        int responseCode = 0;

        if (inputURL == null) {
            ctx.redirect("/");
            return;
        }

//        inputURL = UrlStandardizer.standardize(inputURL);

//        try {
//            URL url = new URL(inputURL);
//            inputURL = inputURL.substring(0, inputURL.length() - url.getPath().length());
//
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setRequestMethod("GET");
//            connection.connect();
//            responseCode = connection.getResponseCode();
//        } catch (Exception e) {
//            System.out.println("Inside of exception");
//            ctx.sessionAttribute("flash", "Не удалось создать статью");
//            ctx.sessionAttribute("flash-type", "danger");
//            ctx.redirect("/");
//            return;
//        }

        System.out.println(inputURL);

        if (new QUrlModel().name.equalTo(inputURL).exists()) {
            System.out.println("It exists");
        }

        UrlModel urlModel = new UrlModel(inputURL);
        urlModel.setResponseCode(responseCode);
        System.out.println(urlModel);
        urlModel.save();
        URL url = new URL(new QUrlModel().id.equalTo(2).findOne().getName());
        System.out.println("This is test Model " + url);


//        ctx.sessionAttribute("flash", "Сайт успешно добавлен");
//        ctx.sessionAttribute("flash-type", "success");








//        String name = ctx.formParam("name");
//        String description = ctx.formParam("description");
//
//        UrlModel article = new UrlModel(name, description);
//
//        if (name.isEmpty() || description.isEmpty()) {
//            ctx.sessionAttribute("flash", "Не удалось создать статью");
//            ctx.sessionAttribute("flash-type", "danger");
//            ctx.attribute("article", article);
//            ctx.render("articles/new.html");
//            return;
//        }
//
//        article.save();
//
//        ctx.sessionAttribute("flash", "Статья успешно создана");
//        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
    };

    public static Handler showArticle = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        UrlModel article = new QUrlModel()
                .id.equalTo(id)
                .findOne();

        if (article == null) {
            throw new NotFoundResponse();
        }

        ctx.attribute("article", article);
        ctx.render("articles/show.html");
    };
}
