package src.service;

import io.restassured.module.jsv.JsonSchemaValidationException;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.json.JSONObject;
import src.config.ConnectionConfig;
import src.pojo.CreateFolderQueryRequest;
import src.util.FileUtil;
import src.util.RestQuerySender;

public class FolderEntity {

    private final String nameFolder;
    private final String url;
    private final String nameToken;
    private final RestQuerySender.TypeRequest method;
    private final String connection;
    private final String pathFolder;
    private Boolean hasToken;
    private String token;

    public FolderEntity(CreateFolderQueryRequest createFolderQueryRequest) {
        nameFolder = createFolderQueryRequest.getNameFolder();
        url = createFolderQueryRequest.getUrl();
        nameToken = createFolderQueryRequest.getNameToken();
        method = RestQuerySender.TypeRequest.valueOf(createFolderQueryRequest.getMethod());
        connection = createFolderQueryRequest.getConnection();
        hasToken = false;
        pathFolder = String.format("./data/folder/%s/", nameFolder);

        storeFile(createFolderQueryRequest);
    }

    private void storeFile(CreateFolderQueryRequest createFolderQueryRequest) {
        FileUtil.createFolderFromString(pathFolder);

        FileUtil.createFileFromString(pathFolder, "body.json", createFolderQueryRequest.getBodyText());
        FileUtil.createFileFromString(pathFolder, "expect_response.json", createFolderQueryRequest.getResponseText());
    }

    public void refreshTokenRequest() {
        RestQuerySender restQuerySender = new RestQuerySender();

        String body = FileUtil.fileReader(pathFolder + "expect_response.json");

        JSONObject jsonBody = (body != null && body.isEmpty()) ? new JSONObject(body) : null;

        String connectionUrl = ConnectionConfig.getConnectionUrl(connection);
        Response actualResponse = null;
        try {
            actualResponse = restQuerySender.getResponse(connectionUrl + url, jsonBody, method);
            FileUtil.createFileFromString(pathFolder, connection + "_response.json", actualResponse.asString());
        } catch (Exception ignore) {
        }

        try {
            String strings = FileUtil.fileReader(pathFolder + "expect_response.json");
            if (actualResponse == null)
                throw new AssertionError();
            actualResponse.then().assertThat().body(JsonSchemaValidator.matchesJsonSchema(strings));
            token = actualResponse.then().extract().jsonPath().get(nameToken);
            if (token != null && !token.isEmpty()) {
                hasToken = true;
            }
            System.out.println("Success");
        } catch (AssertionError | JsonSchemaValidationException e) {
            hasToken = false;
            System.out.println("ERROR");
            e.printStackTrace();
        }
    }

    public String getNameFolder() {
        return nameFolder;
    }

    public String getUrl() {
        return url;
    }

    public String getNameToken() {
        return nameToken;
    }

    public String getToken() {
        return token;
    }

    public RestQuerySender.TypeRequest getMethod() {
        return method;
    }

    public String getConnection() {
        return connection;
    }

    public String getPathFolder() {
        return pathFolder;
    }

    public Boolean getHasToken() {
        return hasToken;
    }
}
