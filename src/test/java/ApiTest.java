import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import static io.restassured.RestAssured.given;
import static io.restassured.config.EncoderConfig.encoderConfig;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ApiTest {
    private static final String TOKEN = "fT6zIWx0X1wAAAAAAAAAAfa2hQr47rNsKNEOLn9HBLJuNjy45h8Ynxj_V85PCqrz";
    private static final String PATH_LOCAL = "./file.txt";
    private static final String PATH_CLOUD = "/file.txt";

    @Test
    @Order(1)
    public void upload() throws IOException {
        given().config(RestAssured.config().encoderConfig(encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .header("Authorization", "Bearer " + TOKEN)
                .header("Dropbox-API-Arg",
                        "{\"mode\": \"add\", \"autorename\": true, \"mute\": false, \"path\": \"" + PATH_CLOUD +
                                "\", \"strict_conflict\": false}")
                .header("Content-Type", "application/octet-stream")
                .body(Files.readAllBytes(Paths.get(PATH_LOCAL)))
                .when()
                .post("https://content.dropboxapi.com/2/files/upload");
    }

    @Test
    @Order(2)
    public void getFile() throws IOException {
        JSONObject requestBody = new JSONObject();
        requestBody.put("path", PATH_CLOUD);
        requestBody.put("include_media_info", false);
        requestBody.put("include_deleted", false);
        requestBody.put("include_has_explicit_shared_members", false);
        Response responseWithId = RestAssured
                .given()
                .config(RestAssured
                .config()
                .encoderConfig(encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .header("Authorization", "Bearer " + TOKEN)
                .header("Content-Type", "application/json")
                .body(requestBody.toJSONString())
                .when()
                .post("https://api.dropboxapi.com/2/files/get_metadata");
        System.out.println("1. " + responseWithId.asString());
        String fileId = responseWithId.jsonPath().get("id");
        System.out.println("2. " + fileId);
        JSONObject requestBody2 = new JSONObject();
        requestBody2.put("file", fileId);
        requestBody2.put("actions", new ArrayList());
        RestAssured.given().
                config(RestAssured.config().
                        encoderConfig(encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false))).
                header("Authorization", "Bearer " + TOKEN).
                header("Content-Type", "application/json").
                body(requestBody2.toJSONString()).
                when().
                post("https://api.dropboxapi.com/2/sharing/get_file_metadata").
                then().statusCode(200);
        System.out.println("3. " + requestBody.toJSONString());
    }

    @Test
    @Order(3)
    public void deleteFile() {
        JSONObject requestBody = new JSONObject();
        requestBody.put("path", PATH_CLOUD);
        System.out.println("!!! " + requestBody.toJSONString());
        given().
                config(RestAssured.config()
                        .encoderConfig(encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .header("Authorization", "Bearer " + TOKEN)
                .header("Content-Type", "application/json")
                .body(requestBody.toJSONString()).when()
                .post("https://api.dropboxapi.com/2/files/delete_v2").
                        then().statusCode(200);
    }
}
