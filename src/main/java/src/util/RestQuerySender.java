package src.util;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.JSONObject;

import static io.restassured.RestAssured.given;

public class RestQuerySender {

    public enum TypeRequest {
        POST("POST"),
        PUT("PUT"),
        GET("GET"),
        DELETE("DELETE");
        private final String method;
        TypeRequest(String method){
            this.method = method;
        }
        public String getMethod() {
            return method;
        }
    }

    public static RequestSpecBuilder getResponseBuilder(String url) {
        return new RequestSpecBuilder()
                .setBaseUri("http://" + url)
                .setContentType(Produces.APPLICATION_JSON_UTF8)
                .setAccept(ContentType.JSON);
    }

    public RequestSpecification givenAuth(String token) {
        return given().headers("Authorization","Bearer " + token);
    }

    private Response getBaseResponse(String token, String url, JSONObject body, TypeRequest typeRequest) {
        RequestSpecification requestSpecification;
        if (token != null && !token.isEmpty()) {
            requestSpecification = givenAuth(token);
        } else {
            requestSpecification = given();
        }

        requestSpecification
                .header(new Header("X-Device-Type", "WEB"))
                .spec(getResponseBuilder(url).build())
                .body(body==null ? "" : body.toString())
                .when();

        Response response = null;
        switch (typeRequest) {
            case GET:
                response = requestSpecification.get();
                break;
            case POST:
                response = requestSpecification.post();
                break;
            case PUT:
                response = requestSpecification.put();
                break;
            case DELETE:
                response = requestSpecification.delete();
                break;
            default:
                break;
        }
        return response;
    }

    public Response getResponse(String url, JSONObject body, TypeRequest typeRequest) {
        return getBaseResponse(null, url, body, typeRequest);
    }

    public Response getAuthResponse(String token, String url, JSONObject body, TypeRequest typeRequest) {
        return getBaseResponse(token, url, body, typeRequest);
    }
}
