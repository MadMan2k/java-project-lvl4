package hexlet.code;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class AppTest {

    private static Javalin app;
    private static String baseUrl;
    private static HttpUrl mockUrl;
    private static UrlModel existingUrlModel;
    private static UrlModel mockUrlModel;
    private static Transaction transaction;
    private static MockWebServer mockWebServer;
    private static Map<String, Integer> inputTestValues;

    @Test
    void testInit() {
        assertThat(true).isEqualTo(true);
    }



    @BeforeAll
    public static void beforeAll() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        inputTestValues = mapper.readValue(Paths.get("src/test/resources/inputTestValues.json").toFile(), Map.class);

        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        existingUrlModel = new UrlModel("https://www.example.com");
        existingUrlModel.save();

        mockWebServer = new MockWebServer();
        mockWebServer.enqueue(new MockResponse().setBody("hello from MockServer"));
        mockWebServer.start(inputTestValues.get("MOCK_PORT"));
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
            assertThat(response.getStatus()).isEqualTo(inputTestValues.get("RESPONSE_CODE_200"));
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

            assertThat(response.getStatus()).isEqualTo(inputTestValues.get("RESPONSE_CODE_200"));
            assertThat(body).contains(existingUrlModel.getName());
        }

        @Test
        void testShow() {
            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls/" + existingUrlModel.getId())
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(inputTestValues.get("RESPONSE_CODE_200"));
            assertThat(body).contains(existingUrlModel.getName());
            assertThat(body).contains(String.valueOf(existingUrlModel.getId()));
        }

        @Test
        void testUrlModelAndUrlCheckModelMethods() {
            LocalDateTime localDateTime = LocalDateTime.of(inputTestValues.get("CUSTOM_YEAR"), 1, 1, 1, 0);

            UrlModel urlModel = new UrlModel();
            urlModel.setId(inputTestValues.get("CUSTOM_ID"));
            urlModel.setCreatedAt(localDateTime);
            urlModel.setName("https://www.example1.com");

            UrlCheckModel urlCheckModel = new UrlCheckModel();
            urlCheckModel.setId(inputTestValues.get("CUSTOM_ID"));
            urlCheckModel.setUrlModel(urlModel);
            urlCheckModel.setStatusCode(inputTestValues.get("RESPONSE_CODE_200"));
            urlCheckModel.setTitle("newTitle");
            urlCheckModel.setH1("newH1");
            urlCheckModel.setDescription("newDescription");
            urlCheckModel.setCreatedAt(localDateTime);

            urlModel.setUrlChecks(Arrays.asList(urlCheckModel));

            urlModel.save();

            UrlModel newUrlModel = new QUrlModel().id.equalTo(inputTestValues.get("CUSTOM_ID")).findOne();

            assertThat(newUrlModel.getId()).isEqualTo(Long.valueOf(inputTestValues.get("CUSTOM_ID")));
            assertThat(newUrlModel.getName()).isEqualTo("https://www.example1.com");
            assertThat(newUrlModel.getCreatedAt()).isEqualTo(localDateTime);
            assertThat(newUrlModel.getUrlChecks().get(0).toString())
                    .isEqualTo("UrlCheckModel{id=50, statusCode=200, title='newTitle', "
                            + "h1='newH1', description='null', createdAt=2022-01-01T01:00}");


        }

        @Test
        void testCreate() {
            String inputName = "https://ru.hexlet.io";
            HttpResponse<String> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", inputName)
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(inputTestValues.get("RESPONSE_CODE_302"));
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(inputTestValues.get("RESPONSE_CODE_200"));
            assertThat(body).contains(inputName);
            assertThat(body).contains("The site was successfully added");

            UrlModel actualUrlModel = new QUrlModel()
                    .name.equalTo(inputName)
                    .findOne();

            assertThat(actualUrlModel).isNotNull();
            assertThat(actualUrlModel.getName()).isEqualTo(inputName);
        }

        @Test
        void checkSiteNotFound() {

            Dispatcher dispatcher = new Dispatcher() {
                @NotNull
                @Override
                public MockResponse dispatch(@NotNull RecordedRequest recordedRequest) throws InterruptedException {
                    return new MockResponse().setResponseCode(inputTestValues.get("RESPONSE_CODE_404"));
                }
            };
            mockWebServer.setDispatcher(dispatcher);

            UrlModel mockUrlModelDB = new QUrlModel().name.equalTo(mockUrlModel.getName()).findOne();

            HttpResponse<String> responsePost = Unirest
                    .post(baseUrl + "/urls/" + mockUrlModelDB.getId() + "/checks")
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(inputTestValues.get("RESPONSE_CODE_302"));
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls/" + mockUrlModelDB.getId());

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls/" + mockUrlModelDB.getId())
                    .asString();

            String body = response.getBody();
            assertThat(body).contains("Server connection timeout error. "
                    + "It looks like this site has been working hard and is resting now :(");
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
                    return new MockResponse().setResponseCode(inputTestValues.get("RESPONSE_CODE_200"))
                            .setBody(contentTestPage);
                }
            };
            mockWebServer.setDispatcher(dispatcher);

            UrlModel mockUrlModelDB = new QUrlModel().name.equalTo(mockUrlModel.getName()).findOne();

            HttpResponse<String> responsePost = Unirest
                    .post(baseUrl + "/urls/" + mockUrlModelDB.getId() + "/checks")
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(inputTestValues.get("RESPONSE_CODE_302"));
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls/" + mockUrlModelDB.getId());
            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls/" + mockUrlModelDB.getId())
                    .asString();

            String body = response.getBody();
            assertThat(body).contains("The site was successfully checked");
            assertThat(body).contains("GitHub: Where the world builds software ?? GitHub");
            assertThat(body).contains("Where the world builds software");
            assertThat(body).contains("GitHub is where over 73 million developers shap...");
        }
    }
}
