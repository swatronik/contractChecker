package src.service;

import io.restassured.module.jsv.JsonSchemaValidationException;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import org.json.JSONObject;
import src.config.ConnectionConfig;
import src.controller.FolderController;
import src.pojo.CreateQueryRequest;
import src.util.FileUtil;
import src.util.RestQuerySender;

import java.util.HashMap;

public class QueryEntity {

    private String name;
    private String url;
    private RestQuerySender.TypeRequest method;
    private HashMap<String, Boolean> connections = new HashMap<>();
    private String folderName;
    private String pathFolder;
    private String token;

    public QueryEntity(CreateQueryRequest createQueryRequest) {
        name = createQueryRequest.getName();
        url = createQueryRequest.getUrl();

        method = RestQuerySender.TypeRequest.valueOf(createQueryRequest.getMethod());

        for (String connection: createQueryRequest.getConnections()) {
            connections.put(connection, false);
        }

        folderName = createQueryRequest.getFolderName();
        token = FolderController.folderEntities.stream().filter(el -> el.getNameFolder().equals(folderName)).findFirst().get().getToken();
        pathFolder = String.format("./data/query/%s/%s/", folderName, name);

        storeFile(createQueryRequest);
    }

    public void updateEntity(CreateQueryRequest createQueryRequest) {
        if (createQueryRequest.getName() != null && !createQueryRequest.getName().isEmpty()) {
            name = createQueryRequest.getName();
        }
        if (createQueryRequest.getUrl() != null && !createQueryRequest.getUrl().isEmpty()) {
            url = createQueryRequest.getUrl();
        }
        if (createQueryRequest.getMethod() != null && !createQueryRequest.getMethod().isEmpty()) {
            method = RestQuerySender.TypeRequest.valueOf(createQueryRequest.getMethod());
        }
        if (createQueryRequest.getFolderName() != null && !createQueryRequest.getFolderName().isEmpty()) {
            folderName = createQueryRequest.getFolderName();
        }
        String oldPathFolder = pathFolder;
        pathFolder = String.format("./data/query/%s/%s/", folderName, name);
        if (!oldPathFolder.equals(pathFolder)) {
            FileUtil.move(oldPathFolder, pathFolder);
        }

        if (createQueryRequest.getConnections() != null) {
            connections.clear();
            for (String connection: createQueryRequest.getConnections()) {
                connections.put(connection, false);
            }
        }

        storeFile(createQueryRequest);
    }

    private void storeFile(CreateQueryRequest createQueryRequest) {
        FileUtil.createFolderFromString(pathFolder);
        if (createQueryRequest.getBodyText() != null) {
            FileUtil.createFileFromString(pathFolder, "body.json", createQueryRequest.getBodyText());
        }
        if (createQueryRequest.getResponseText() != null) {
            FileUtil.createFileFromString(pathFolder, "expect_response.json", createQueryRequest.getResponseText());
        }
    }

    public void refreshRequest() {
        RestQuerySender restQuerySender = new RestQuerySender();

        String body = FileUtil.fileReader(pathFolder + "body.json");

        JSONObject jsonBody = (body != null && !body.isEmpty()) ? new JSONObject(body) : null;

        for (String connection: connections.keySet()) {
            String connectionUrl = ConnectionConfig.getConnectionUrl(connection);
            Response actualResponse = null;
            try {
                actualResponse = restQuerySender.getAuthResponse(token,connectionUrl + url, jsonBody, method);
                FileUtil.createFileFromString(pathFolder, connection + "_response.json", actualResponse.asString());
            } catch (Exception ignore){}

            try {
                String strings = FileUtil.fileReader(pathFolder + "expect_response.json");
                if (actualResponse == null)
                    throw new AssertionError();
                actualResponse.then().assertThat().body(JsonSchemaValidator.matchesJsonSchema(strings));
                connections.put(connection, true);
                System.out.println("Success");
            } catch (AssertionError | JsonSchemaValidationException e) {
                connections.put(connection, false);
                System.out.println("ERROR");
                e.printStackTrace();
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public RestQuerySender.TypeRequest getMethod() {
        return method;
    }

    public HashMap<String, Boolean> getConnections() {
        return connections;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getPathFolder() {
        return pathFolder;
    }
}
