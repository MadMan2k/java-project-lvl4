package hexlet.code;

import hexlet.code.model.UrlCheckModel;
import hexlet.code.model.UrlModel;
import hexlet.code.model.query.QUrlModel;
import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


public class AppTest {

    private static final int RESPONSE_CODE_200 = 200;
    private static final int RESPONSE_CODE_302 = 302;

    @Test
    void testInit() {
        assertThat(true).isEqualTo(true);
    }

    private static Javalin app;
    private static String baseUrl;
    private static HttpUrl mockUrl;
    private static UrlModel existingUrlModel;
    private static UrlModel mockUrlModel;
    private static Transaction transaction;
    private static MockWebServer mockWebServer;

    @BeforeAll
    public static void beforeAll() throws IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        existingUrlModel = new UrlModel("https://www.example.com");
        existingUrlModel.save();

        mockWebServer = new MockWebServer();
        mockWebServer.enqueue(new MockResponse().setBody("hello from MockServer"));
        mockWebServer.start(7000);
        mockUrl = mockWebServer.url("/");
        mockUrlModel = new UrlModel(mockUrl.toString());
        mockUrlModel.save();


    }

    @AfterAll
    public static void afterAll() throws IOException {
        mockWebServer.shutdown();
        app.stop();
    }

    /**
     * Open transaction before each test.
     */
    @BeforeEach
    void beforeEach() {
        transaction = DB.beginTransaction();
    }

    /**
     * Close transaction after each test.
     */
    @AfterEach
    void afterEach() {
        transaction.rollback();
    }

    @Nested
    class RootTest {

        @Test
        void testIndex() {
            HttpResponse<String> response = Unirest.get(baseUrl).asString();
            assertThat(response.getStatus()).isEqualTo(RESPONSE_CODE_200);
            assertThat(response.getBody()).contains("Free website SEO checker");
        }
    }


    @Nested
    class URLsTest {

        @Test
        void testIndex() {
            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(RESPONSE_CODE_200);
            assertThat(body).contains(existingUrlModel.getName());
        }

        @Test
        void testShow() {
            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls/" + existingUrlModel.getId())
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(RESPONSE_CODE_200);
            assertThat(body).contains(existingUrlModel.getName());
            assertThat(body).contains(String.valueOf(existingUrlModel.getId()));
        }

        @Test
        void testCreate() {
            String inputName = "ru.hexlet.io";
            HttpResponse<String> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputName)
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(RESPONSE_CODE_302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(RESPONSE_CODE_200);
            assertThat(body).contains("https://" + inputName);
            assertThat(body).contains("The site was successfully added");

            UrlModel actualUrlModel = new QUrlModel()
                    .name.equalTo("https://" + inputName)
                    .findOne();

            assertThat(actualUrlModel).isNotNull();
            assertThat(actualUrlModel.getName()).isEqualTo("https://" + inputName);
        }

        @Test
        void checkSiteNotFound() {

            Dispatcher dispatcher = new Dispatcher() {
                @NotNull
                @Override
                public MockResponse dispatch(@NotNull RecordedRequest recordedRequest) throws InterruptedException {
                    return new MockResponse().setResponseCode(404);
                }
            };
            mockWebServer.setDispatcher(dispatcher);

            UrlModel mockUrlModelDB = new QUrlModel().name.equalTo(mockUrlModel.getName()).findOne();

            HttpResponse<String> responsePost = Unirest
                    .post(baseUrl + "/urls/" + mockUrlModelDB.getId() + "/check")
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(RESPONSE_CODE_302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls/" + mockUrlModelDB.getId());

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls/" + mockUrlModelDB.getId())
                    .asString();

            String body = response.getBody();
            assertThat(body).contains("Server connection timeout error. It looks like this site has been working hard and is resting now :(");
        }

        @Test
        void testCheckSiteSuccess() {
            StringBuilder contentBuilder = new StringBuilder();
            try {
                BufferedReader in = new BufferedReader(new FileReader("src/test/resources/testPage.html"));
                String str;
                while ((str = in.readLine()) != null) {
                    contentBuilder.append(str);
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String contentTestPage = contentBuilder.toString();

            Dispatcher dispatcher = new Dispatcher() {
                @NotNull
                @Override
                public MockResponse dispatch(@NotNull RecordedRequest recordedRequest) throws InterruptedException {
                    return new MockResponse().setResponseCode(200).setBody(contentTestPage);
                }
            };
            mockWebServer.setDispatcher(dispatcher);

            UrlModel mockUrlModelDB = new QUrlModel().name.equalTo(mockUrlModel.getName()).findOne();

            HttpResponse<String> responsePost = Unirest
                    .post(baseUrl + "/urls/" + mockUrlModelDB.getId() + "/check")
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(RESPONSE_CODE_302);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls/" + mockUrlModelDB.getId());
            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls/" + mockUrlModelDB.getId())
                    .asString();

            String body = response.getBody();
            System.out.println(body);
            assertThat(body).contains("The site was successfully checked");
            assertThat(body).contains("GitHub: Where the world builds software · GitHub");
            assertThat(body).contains("Where the world builds software");
            assertThat(body).contains("GitHub is where over 73 million developers shap...");
        }
    }
}