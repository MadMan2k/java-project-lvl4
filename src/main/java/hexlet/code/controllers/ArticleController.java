package hexlet.code.controllers;

import hexlet.code.model.UrlModel;
import hexlet.code.model.query.QUrlModel;
import hexlet.code.service.UrlStandardizer;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ArticleController {
    private static final int ROWS_PER_PAGE = 10;

    public static Handler getListArticles() {
        return listArticles;
    }

    public static Handler getCreateArticle() {
        return createArticle;
    }

    public static Handler getShowArticle() {
        return showArticle;
    }

    private static Handler listArticles = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;

        PagedList<UrlModel> pagedUrlModels = new QUrlModel()
                .setFirstRow(page * ROWS_PER_PAGE)
                .setMaxRows(ROWS_PER_PAGE)
                .orderBy()
                .id.desc()
                .findPagedList();

        List<UrlModel> urlModels = pagedUrlModels.getList();

        int lastPage = pagedUrlModels.getTotalPageCount() + 1;
        int currentPage = pagedUrlModels.getPageIndex() + 1;
        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .collect(Collectors.toList());

        ctx.attribute("urlModels", urlModels);
        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);
        ctx.render("URLs/index.html");
    };

    // Handler not exist in my app
//    public static Handler newArticle = ctx -> {
//        UrlModel article = new UrlModel();
//
//        ctx.attribute("article", article);
//        ctx.render("articles/new.html");
//    };

    private static Handler createArticle = ctx -> {
        String inputURL = ctx.formParam("url");
        int responseCode = 0;

        if (inputURL == null) {
            ctx.redirect("/");
            return;
        }

        inputURL = UrlStandardizer.standardize(inputURL);

        if (new QUrlModel().name.equalTo(inputURL).exists()) {
            ctx.sessionAttribute("flash", "Сайт уже существует в базе");
            ctx.sessionAttribute("flash-type", "info");
            ctx.redirect("/urls");
            return;
        }

        try {
            URL url = new URL(inputURL);
            inputURL = inputURL.substring(0, inputURL.length() - url.getPath().length());

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            responseCode = connection.getResponseCode();
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }

        UrlModel urlModel = new UrlModel(inputURL);
        urlModel.setResponseCode(responseCode);
        urlModel.save();

        ctx.sessionAttribute("flash", "Сайт успешно добавлен");
        ctx.sessionAttribute("flash-type", "success");

        ctx.redirect("/urls");
    };

    private static Handler showArticle = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        UrlModel urlModel = new QUrlModel()
                .id.equalTo(id)
                .findOne();

        if (urlModel == null) {
            throw new NotFoundResponse();
        }

        ctx.attribute("urlModel", urlModel);
        ctx.render("URLs/show.html");
    };
}
