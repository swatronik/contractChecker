package src.controller;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import src.config.ConnectionConfig;
import src.pojo.ConnectionQueryRequest;
import src.pojo.CreateFolderQueryRequest;
import src.pojo.CreateQueryRequest;
import src.pojo.ExportDataRequest;
import src.service.FolderEntity;
import src.service.QueryEntity;
import src.util.FileUtil;
import src.util.MediaTypeUtils;
import src.util.Produces;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@Controller
public class SaveController {

    private static final String DEFAULT_FILE_NAME = "data/query/%s/%s";
    private static final String DEFAULT_7Z_DIR = "data/archive/";

    private final Gson gson;

    @Autowired
    public SaveController(Gson gson) {
        this.gson = gson;
    }

    @Autowired
    private ServletContext servletContext;

    @RequestMapping("/export")
    public ResponseEntity<InputStreamResource> downloadExport() throws IOException {
        ExportDataRequest exportDataRequest = new ExportDataRequest();

        exportDataRequest.setRunPeriod(PeriodCheckController.getCronPeriod());

        ArrayList<ConnectionQueryRequest> arrayListConnections = new ArrayList<>();
        HashMap<String, String> connections = ConnectionConfig.getConnections();
        for(String key: connections.keySet()) {
            ConnectionQueryRequest connectionQueryRequest = new ConnectionQueryRequest();
            connectionQueryRequest.setNameConnection(key);
            connectionQueryRequest.setUrlConnection(connections.get(key));
            arrayListConnections.add(connectionQueryRequest);
        }
        exportDataRequest.setConnections(arrayListConnections);

        ArrayList<CreateFolderQueryRequest> arrayListFolder = new ArrayList<>();
        for(FolderEntity folderEntity: FolderController.folderEntities) {
            CreateFolderQueryRequest createFolderQueryRequest = new CreateFolderQueryRequest();
            createFolderQueryRequest.setNameFolder(folderEntity.getNameFolder());
            createFolderQueryRequest.setUrl(folderEntity.getUrl());
            createFolderQueryRequest.setConnection(folderEntity.getConnection());
            createFolderQueryRequest.setMethod(folderEntity.getMethod().getMethod());
            createFolderQueryRequest.setNameToken(folderEntity.getNameToken());
            createFolderQueryRequest.setBodyText(FileUtil.fileReader(String.format("./data/folder/%s/body.json", folderEntity.getNameFolder())));
            createFolderQueryRequest.setResponseText(FileUtil.fileReader(String.format("./data/folder/%s/expect_response.json", folderEntity.getNameFolder())));
            arrayListFolder.add(createFolderQueryRequest);
        }
        exportDataRequest.setFolders(arrayListFolder);

        ArrayList<CreateQueryRequest> arrayListQueries = new ArrayList<>();
        HashMap<String, HashMap<String, QueryEntity>> queryEntities = QueryController.queryEntities;
        for(String folder: queryEntities.keySet()) {
            for (String nameQuery: queryEntities.get(folder).keySet()) {
                QueryEntity queryEntity = queryEntities.get(folder).get(nameQuery);
                CreateQueryRequest createQueryRequest = new CreateQueryRequest();
                createQueryRequest.setFolderName(folder);
                createQueryRequest.setName(nameQuery);
                createQueryRequest.setUrl(queryEntity.getUrl());
                createQueryRequest.setConnections(new ArrayList<>(queryEntity.getConnections().keySet()));
                createQueryRequest.setMethod(queryEntity.getMethod().getMethod());
                createQueryRequest.setBodyText(FileUtil.fileReader(String.format("./data/query/%s/%s/body.json", folder, nameQuery)));
                createQueryRequest.setResponseText(FileUtil.fileReader(String.format("./data/query/%s/%s/expect_response.json", folder, nameQuery)));
                arrayListQueries.add(createQueryRequest);
            }
        }
        exportDataRequest.setQueries(arrayListQueries);

        FileUtil.createFolderFromString("./data/export");
        FileUtil.createFileFromString("./data/export", "export.txt", gson.toJson(exportDataRequest));

        MediaType mediaType = MediaTypeUtils.getMediaTypeForFileName(this.servletContext, "export.txt");

        File file = new File("./data/export/export.txt");
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName())
                .contentType(mediaType)
                .contentLength(file.length())
                .body(resource);
    }

    @PostMapping(
            value = "/import",
            headers = {"Content-Type=text/plain;charset=UTF-8"},
            produces = Produces.APPLICATION_JSON_UTF8)
    public ResponseEntity<Void> doImport(@RequestBody String body) throws IOException {
        ExportDataRequest exportDataRequest = gson.fromJson(body, ExportDataRequest.class);

        ConnectionConfig.getConnections().clear();
        FolderController.folderEntities.clear();
        QueryController.queryEntities.clear();

        PeriodCheckController.setCronPeriod(exportDataRequest.getRunPeriod());

        for(ConnectionQueryRequest connectionQueryRequest: exportDataRequest.getConnections()) {
            ConnectionConfig.setConnection(connectionQueryRequest.getNameConnection(), connectionQueryRequest.getUrlConnection());
        }

        ArrayList<FolderEntity> folderEntities = FolderController.folderEntities;
        for(CreateFolderQueryRequest createFolderQueryRequest: exportDataRequest.getFolders()) {
            FolderEntity folderEntity = new FolderEntity(createFolderQueryRequest);
            folderEntity.refreshTokenRequest();
            folderEntities.add(folderEntity);
        }

        HashMap<String, HashMap<String, QueryEntity>> queryEntities = QueryController.queryEntities;
        for(CreateQueryRequest createQueryRequest: exportDataRequest.getQueries()) {
            QueryEntity queryEntity = new QueryEntity(createQueryRequest);

            queryEntities.computeIfAbsent(createQueryRequest.getFolderName(), k -> new HashMap<>());
            queryEntities.get(createQueryRequest.getFolderName()).put(createQueryRequest.getName(), queryEntity);

            queryEntities.get(createQueryRequest.getFolderName()).get(createQueryRequest.getName()).refreshRequest();
        }

        QueryController.refreshAllQuery();

        return ResponseEntity
                .ok()
                .body(null);
    }
}
