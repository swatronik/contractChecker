package src.controller;

import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import src.pojo.CreateQueryRequest;
import src.pojo.FolderQueryRequest;
import src.pojo.UpdateQueryRequest;
import src.service.FolderEntity;
import src.service.QueryEntity;
import src.util.Produces;

import java.io.IOException;
import java.util.HashMap;

@Controller
public class QueryController {

    private final Gson gson;

    public static final HashMap<String, HashMap<String, QueryEntity>> queryEntities = new HashMap<>();

    @Autowired
    public QueryController(Gson gson) {
        this.gson = gson;
    }

    @PostMapping(
            value = "/createQuery",
            headers = {"Content-Type=text/plain;charset=UTF-8"},
            produces = Produces.APPLICATION_JSON_UTF8)
    public ResponseEntity<Void> createQuery(@RequestBody String body) throws IOException {
        CreateQueryRequest createQueryRequest = gson.fromJson(body, CreateQueryRequest.class);

        QueryEntity queryEntity = new QueryEntity(createQueryRequest);

        queryEntities.computeIfAbsent(createQueryRequest.getFolderName(), k -> new HashMap<>());
        queryEntities.get(createQueryRequest.getFolderName()).put(createQueryRequest.getName(), queryEntity);

        queryEntities.get(createQueryRequest.getFolderName()).get(createQueryRequest.getName()).refreshRequest();

        return ResponseEntity
                .ok()
                .body(null);
    }

    @GetMapping(
            value = "/getQueries",
            headers = {"Accept=*/*"},
            produces = Produces.APPLICATION_JSON_UTF8)
    public ResponseEntity<String> getQueries() {
        JSONArray body = new JSONArray();

        for(String folderName: queryEntities.keySet()) {
            HashMap<String, QueryEntity> queryIntegration = queryEntities.get(folderName);
            for(String nameQuery: queryIntegration.keySet()) {
                QueryEntity entity = queryIntegration.get(nameQuery);

                JSONArray connections = new JSONArray();
                for(String connection: entity.getConnections().keySet()) {
                    connections.put(new JSONObject()
                            .put("name", connection)
                            .put("isConnect", entity.getConnections().get(connection))
                    );
                }
                body.put(new JSONObject()
                        .put("name", entity.getName())
                        .put("url", entity.getUrl())
                        .put("method", entity.getMethod())
                        .put("connections", connections)
                );
            }
        }

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body.toString());
    }

    @PostMapping(
            value = "/getQueryFolder",
            headers = {"Accept=*/*"},
            produces = Produces.APPLICATION_JSON_UTF8)
    public ResponseEntity<String> getQueryFolder(@RequestBody String body) {
        JSONArray jsonArray = new JSONArray();

        FolderQueryRequest folderQueryRequest = gson.fromJson(body, FolderQueryRequest.class);

        HashMap<String, QueryEntity> queryIntegration = queryEntities.get(folderQueryRequest.getNameFolder());
        for (String nameQuery : queryIntegration.keySet()) {
            QueryEntity entity = queryIntegration.get(nameQuery);

            JSONArray connections = new JSONArray();
            for (String connection : entity.getConnections().keySet()) {
                connections.put(new JSONObject()
                        .put("name", connection)
                        .put("isConnect", entity.getConnections().get(connection))
                );
            }
            jsonArray.put(new JSONObject()
                    .put("name", entity.getName())
                    .put("url", entity.getUrl())
                    .put("method", entity.getMethod())
                    .put("connections", connections)
            );
        }

        return ResponseEntity
                .ok()
                .body(jsonArray.toString());
    }

    @PostMapping(
            value = "/getQuery",
            headers = {"Accept=*/*"},
            produces = Produces.APPLICATION_JSON_UTF8)
    public ResponseEntity<String> getQuery(@RequestBody String body) {
        JSONObject jsonObject = new JSONObject();
        FolderQueryRequest folderQueryRequest = gson.fromJson(body, FolderQueryRequest.class);

        HashMap<String, QueryEntity> queryIntegration = queryEntities.get(folderQueryRequest.getNameFolder());
        QueryEntity entity = queryIntegration.get(folderQueryRequest.getNameQuery());

        JSONArray connections = new JSONArray();
        for (String connection : entity.getConnections().keySet()) {
            connections.put(new JSONObject()
                    .put("name", connection)
                    .put("isConnect", entity.getConnections().get(connection))
            );
        }
        jsonObject.put("name", entity.getName())
                .put("url", entity.getUrl())
                .put("method", entity.getMethod())
                .put("connections", connections);

        return ResponseEntity
                .ok()
                .body(jsonObject.toString());
    }

    @PostMapping(
            value = "/deleteQuery",
            headers = {"Accept=*/*"},
            produces = Produces.APPLICATION_JSON_UTF8)
    public ResponseEntity<String> deleteQuery(@RequestBody String body) {
        FolderQueryRequest folderQueryRequest = gson.fromJson(body, FolderQueryRequest.class);

        HashMap<String, QueryEntity> queryIntegration = queryEntities.get(folderQueryRequest.getNameFolder());
        queryIntegration.remove(folderQueryRequest.getNameQuery());

        return ResponseEntity
                .ok()
                .body("");
    }

    @PostMapping(
            value = "/updateQuery",
            headers = {"Accept=*/*"},
            produces = Produces.APPLICATION_JSON_UTF8)
    public ResponseEntity<String> updateQuery(@RequestBody String body) {
        UpdateQueryRequest updateQueryRequest = gson.fromJson(body, UpdateQueryRequest.class);

        HashMap<String, QueryEntity> queryIntegration = queryEntities.get(updateQueryRequest.getNameFolder());
        QueryEntity entity = queryIntegration.get(updateQueryRequest.getNameQuery());

        queryIntegration.remove(updateQueryRequest.getNameQuery());
        entity.updateEntity(updateQueryRequest.getUpdateObject());
        queryEntities.computeIfAbsent(entity.getFolderName(), k -> new HashMap<>());
        queryEntities.get(entity.getFolderName()).put(entity.getName(), entity);

        return ResponseEntity
                .ok()
                .body("");
    }

    @PostMapping(
            value = "/refreshQuery",
            headers = {"Accept=*/*"},
            produces = Produces.APPLICATION_JSON_UTF8)
    public ResponseEntity<String> refreshQuery(@RequestBody String body) {
        FolderQueryRequest folderQueryRequest = gson.fromJson(body, FolderQueryRequest.class);

        for(FolderEntity entity: FolderController.folderEntities) {
            if (entity.getNameFolder().equals(folderQueryRequest.getNameFolder())) {
                entity.refreshTokenRequest();
                break;
            }
        }

        queryEntities
                .get(folderQueryRequest.getNameFolder())
                .get(folderQueryRequest.getNameQuery())
                .refreshRequest();

        return ResponseEntity
                .ok()
                .body("");
    }

    @GetMapping(
            value = "/refreshAllQuery",
            headers = {"Accept=*/*"},
            produces = Produces.APPLICATION_JSON_UTF8)
    public static ResponseEntity<String> refreshAllQuery() {

        for(FolderEntity entity: FolderController.folderEntities) {
            entity.refreshTokenRequest();
        }

        for (String folderName : queryEntities.keySet()) {
            HashMap<String, QueryEntity> folderEntity = queryEntities.get(folderName);
            for (String nameQuery : folderEntity.keySet()) {
                queryEntities
                        .get(folderName)
                        .get(nameQuery)
                        .refreshRequest();
            }
        }


        return ResponseEntity
                .ok()
                .body("");
    }

}
