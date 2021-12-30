package hexlet.code;

import hexlet.code.model.UrlModel;
import hexlet.code.model.query.QUrlModel;
import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
    private static UrlModel existingUrlModel;
    private static Transaction transaction;

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;

        existingUrlModel = new UrlModel("https://www.example.com");
        existingUrlModel.save();
    }

    @AfterAll
    public static void afterAll() {
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
//
//        @Test
//        void testNew() {
//            HttpResponse<String> response = Unirest
//                    .get(baseUrl + "/articles/new")
//                    .asString();
//            String body = response.getBody();
//
//            assertThat(response.getStatus()).isEqualTo(200);
//        }
//
        @Test
        void testCreate() {
            String inputName = "https://www.youtube.com";
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
            assertThat(body).contains(inputName);
            assertThat(body).contains("The site was successfully added");

            UrlModel actualUrlModel = new QUrlModel()
                    .name.equalTo(inputName)
                    .findOne();

            assertThat(actualUrlModel).isNotNull();
            assertThat(actualUrlModel.getName()).isEqualTo(inputName);
        }
    }
}
