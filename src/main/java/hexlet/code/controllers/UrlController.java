package hexlet.code.controllers;

import hexlet.code.model.UrlCheckModel;
import hexlet.code.model.UrlModel;
import hexlet.code.model.query.QUrlModel;
import io.ebean.PagedList;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class UrlController {
    private static final int ROWS_PER_PAGE = 10;
    private static final int TIMEOUT_LIMIT = 10000;
    private static final int NOT_FOUND_CODE = 404;

    public static void getListOfURLs(Context ctx) {
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
    }

    public static void getCreateURL(Context ctx) {
        String inputURL = ctx.formParam("url");

        if (inputURL == null) {
            ctx.redirect("/");
            return;
        }

        String normalizedURL = getNormalizedURL(inputURL);

        if ("Invalid URL".equals(normalizedURL)) {
            ctx.sessionAttribute("flash", "Invalid URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }

        if (new QUrlModel().name.equalTo(inputURL).exists()) {
            ctx.sessionAttribute("flash", "The site already exists in the database");
            ctx.sessionAttribute("flash-type", "info");
            ctx.redirect("/urls");
            return;
        }

        UrlModel urlModel = new UrlModel(inputURL);
        urlModel.save();

        String flashRus = " / Страница успешно добавлена";

        ctx.sessionAttribute("flash", "The site was successfully added" + flashRus);
        ctx.sessionAttribute("flash-type", "success");

        ctx.redirect("/urls");
    }

    private static String getNormalizedURL(String inputURL) {
        inputURL = inputURL.toLowerCase(Locale.ROOT);

        URL url;
        try {
            url = new URL(inputURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "Invalid URL";
        }
        String normalizedURL = url.getProtocol() + "://" + url.getHost();
        if (url.getPort() != -1) {
            normalizedURL = normalizedURL + ":" + url.getPort();
        }

        return normalizedURL;

    }

    public static void getShowURL(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        UrlModel urlModel = new QUrlModel()
                .id.equalTo(id)
                .findOne();

        if (urlModel == null) {
            throw new NotFoundResponse();
        }

        ctx.attribute("urlModel", urlModel);
        ctx.render("URLs/show.html");
    }

    public static void getCheckURL(Context ctx) {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        UrlModel urlModel = new QUrlModel()
                .id.equalTo(id)
                .findOne();

        if (urlModel == null) {
            throw new NotFoundResponse();
        }

        String urlAsString = urlModel.getName();

        int statusCode;
//        Document doc;
        Connection.Response response;
        UrlCheckModel urlCheckModel;
        try {
            response = Jsoup.connect(urlAsString)
                    .timeout(TIMEOUT_LIMIT)
                    .execute();

            urlCheckModel = createUrlCheckModel(response);


//            doc = response.parse();
//            statusCode = response.statusCode();
        } catch (Exception e) {
            if (e instanceof HttpStatusException) {
                statusCode = ((HttpStatusException) e).getStatusCode();
            } else {
                statusCode = NOT_FOUND_CODE;
            }

            urlCheckModel = new UrlCheckModel(statusCode, "", "", "");
            urlModel.addCheckToUrl(urlCheckModel);
            urlModel.save();
            ctx.attribute("urlModel", urlModel);
            ctx.sessionAttribute("flash", "Server connection timeout error. "
                    + "It looks like this site has been working hard and is resting now :(");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/urls/" + urlModel.getId());
            return;
        }

//        String title;
//        try {
//            title = doc.title();
//        } catch (Exception e) {
//            title = "";
//        }
//
//        String h1;
//        try {
//            h1 = doc.select("h1").first().text();
//        } catch (Exception e) {
//            h1 = "";
//        }
//
//        String description;
//        try {
//            description = doc.select("meta[name=description]").get(0).attr("content");
//        } catch (Exception e) {
//            description = "";
//        }

//        UrlCheckModel urlCheckModel = new UrlCheckModel(statusCode, title, h1, description);

        urlModel.addCheckToUrl(urlCheckModel);
        urlModel.save();

        ctx.attribute("urlModel", urlModel);

        String flashRus = " / Страница успешно проверена";

        ctx.sessionAttribute("flash", "The site was successfully checked" + flashRus);
        ctx.sessionAttribute("flash-type", "info");
        ctx.redirect("/urls/" + urlModel.getId());
    }

    private static UrlCheckModel createUrlCheckModel(Connection.Response response) throws IOException {
        int statusCode  = response.statusCode();;
        Document doc;
        doc = response.parse();

        String title;
        try {
            title = doc.title();
        } catch (Exception e) {
            title = "";
        }

        String h1;
        try {
            h1 = doc.select("h1").first().text();
        } catch (Exception e) {
            h1 = "";
        }

        String description;
        try {
            description = doc.select("meta[name=description]").get(0).attr("content");
        } catch (Exception e) {
            description = "";
        }

        return new UrlCheckModel(statusCode, title, h1, description);
    }
}
