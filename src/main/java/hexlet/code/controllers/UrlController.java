package hexlet.code.controllers;

import hexlet.code.model.UrlCheckModel;
import hexlet.code.model.UrlModel;
import hexlet.code.model.query.QUrlModel;
import hexlet.code.service.UrlStandardizer;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class UrlController {
    private static final int ROWS_PER_PAGE = 10;
    private static final int TIMEOUT_LIMIT = 10000;

    public static Handler getListOfURLs() {
        return listOfURLs;
    }

    public static Handler getCreateURL() {
        return createURL;
    }

    public static Handler getShowURL() {
        return showURL;
    }

    public static Handler getCheckURL() {
        return checkURL;
    }

    private static Handler listOfURLs = ctx -> {
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

    private static Handler createURL = ctx -> {
        String inputURL = ctx.formParam("url");
        int responseCode = 0;

        if (inputURL == null) {
            ctx.redirect("/");
            return;
        }

        inputURL = UrlStandardizer.standardize(inputURL);

        if (new QUrlModel().name.equalTo(inputURL).exists()) {
            ctx.sessionAttribute("flash", "The site already exists in the database");
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
            ctx.sessionAttribute("flash", "Invalid URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }

        UrlModel urlModel = new UrlModel(inputURL);
//        urlModel.setResponseCode(responseCode);
        urlModel.save();

        ctx.sessionAttribute("flash", "The site was successfully added");
        ctx.sessionAttribute("flash-type", "success");

        ctx.redirect("/urls");
    };

    private static Handler showURL = ctx -> {
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

    private static Handler checkURL = ctx -> {
        String title;
        String h1;
        String description;
        int statusCode;
        Document doc;
        Connection.Response response;

        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);





        UrlModel urlModel = new QUrlModel()
                .id.equalTo(id)
                .findOne();

        if (urlModel == null) {
            throw new NotFoundResponse();
        }

        String urlAsString = urlModel.getName();



//        UrlCheckModel urlCheckModel = new UrlCheckModel();
//        Random random = new Random();
//        urlCheckModel.setStatusCode(random.ints(100, 999).findFirst().getAsInt());
//        urlCheckModel.setTitle("This is the title");
//        urlCheckModel.setH1("This is H1");
//        urlCheckModel.setDescription("This is description");

        try {
            response = Jsoup.connect(urlAsString)
                    .timeout(TIMEOUT_LIMIT)
                    .execute();
            doc = response.parse();
            statusCode = response.statusCode();
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Server connection timeout error. Looks like the site is down :(");
            ctx.sessionAttribute("flash-type", "alert");
            ctx.render("URLs/show.html");
            return;
        }

        try {
            title = doc.title();
        } catch (Exception e) {
            title = "";
        }

        try {
            h1 = doc.select("h1").first().text();
        } catch (Exception e) {
            h1 = "";
        }

        try {
            description = doc.select("meta[name=description]").get(0).attr("content");
        } catch (Exception e) {
            description = "";
        }

//        System.out.println("response code : " + statusCode);
//        System.out.println("title : " + title);
//        System.out.println("h1 : " + h1);
//        System.out.println("description : " + description);

        UrlCheckModel urlCheckModel = new UrlCheckModel(statusCode, title, h1, description);

        urlModel.addCheckToUrl(urlCheckModel);
        urlModel.save();

        ctx.attribute("urlModel", urlModel);
        ctx.sessionAttribute("flash", "The site was successfully checked");
        ctx.sessionAttribute("flash-type", "info");
        ctx.render("URLs/show.html");
    };
}
