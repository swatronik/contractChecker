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
import src.pojo.CreateFolderQueryRequest;
import src.pojo.FolderQueryRequest;
import src.service.FolderEntity;
import src.service.QueryEntity;
import src.util.Produces;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@Controller
public class FolderController {

    public static final ArrayList<FolderEntity> folderEntities = new ArrayList<>();

    private final Gson gson;

    @Autowired
    public FolderController(Gson gson) {
        this.gson = gson;
    }

    @PostMapping(
            value = "/createFolder",
            headers = {"Content-Type=text/plain;charset=UTF-8"},
            produces = Produces.APPLICATION_JSON_UTF8)
    public ResponseEntity<Void> createFolder(@RequestBody String body) throws IOException {
        CreateFolderQueryRequest createFolderQueryRequest = gson.fromJson(body, CreateFolderQueryRequest.class);

        FolderEntity folderEntity = new FolderEntity(createFolderQueryRequest);
        folderEntity.refreshTokenRequest();

        folderEntities.add(folderEntity);

        return ResponseEntity
                .ok()
                .body(null);
    }

    @GetMapping(
            value = "/getFolders",
            headers = {"Accept=*/*"},
            produces = Produces.APPLICATION_JSON_UTF8)
    public ResponseEntity<String> getFolders() {
        JSONArray body = new JSONArray();

        for (FolderEntity folderEntity : folderEntities) {
            Boolean hasConnection = null;
            HashMap<String, QueryEntity> queryIntegration = QueryController.queryEntities.get(folderEntity.getNameFolder());
            if (queryIntegration != null) {
                for (String nameQuery : queryIntegration.keySet()) {
                    QueryEntity entity = queryIntegration.get(nameQuery);
                    if (hasConnection == null) hasConnection = true;
                    for (String connection : entity.getConnections().keySet()) {
                        if (!entity.getConnections().get(connection)) {
                            hasConnection = false;
                        }
                    }

                }
            }

            body.put(new JSONObject()
                    .put("nameFolder", folderEntity.getNameFolder())
                    .put("haveToken", folderEntity.getHasToken())
                    .put("haveConnection", hasConnection)
            );
        }

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body.toString());
    }

    @PostMapping(
            value = "/deleteFolder",
            headers = {"Content-Type=text/plain;charset=UTF-8"},
            produces = Produces.APPLICATION_JSON_UTF8)
    public ResponseEntity<String> deleteFolder(@RequestBody String body) throws IOException {
        FolderQueryRequest folderQueryRequest = gson.fromJson(body, FolderQueryRequest.class);

        for(FolderEntity entity: folderEntities) {
            if (entity.getNameFolder().equals(folderQueryRequest.getNameFolder())) {
                folderEntities.remove(entity);
                break;
            }
        }

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body("");
    }
}
