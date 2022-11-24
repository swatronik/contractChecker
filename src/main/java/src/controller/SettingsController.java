package src.controller;

import com.google.gson.Gson;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import src.config.ConnectionConfig;
import src.pojo.ConnectionQueryRequest;
import src.util.Produces;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

@RestController
public class SettingsController {

    private final Gson gson;

    @Autowired
    public SettingsController(Gson gson) {
        this.gson = gson;
    }

    @PostMapping(
            value = "/saveConnection",
            headers = {"Content-Type=text/plain;charset=UTF-8"},
            produces = Produces.APPLICATION_JSON_UTF8)
    public ResponseEntity<String> createQuery(@RequestBody String body) throws IOException {
        ConnectionQueryRequest connectionQueryRequest = gson.fromJson(body, ConnectionQueryRequest.class);

        if (!ConnectionConfig.isExist(connectionQueryRequest.getNameConnection())) {
            ConnectionConfig.setConnection(connectionQueryRequest.getNameConnection(), connectionQueryRequest.getUrlConnection());
        } else {
            return ResponseEntity
                    .badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("No unique name connection");
        }

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body("");
    }

    @GetMapping(
            value = "/getConnection",
            headers = {"Accept=*/*"},
            produces = Produces.APPLICATION_JSON_UTF8)
    public ResponseEntity<String> getConnection() {

        JSONArray body = new JSONArray();

        body.put(ConnectionConfig.getConnections());

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(body.toString());
    }

    @PostMapping(
            value = "/checkConnect",
            headers = {"Content-Type=text/plain;charset=UTF-8"},
            produces = Produces.APPLICATION_JSON_UTF8)
    public ResponseEntity<String> checkConnect(@RequestBody String body) throws IOException {
        ConnectionQueryRequest connectionQueryRequest = gson.fromJson(body, ConnectionQueryRequest.class);

        boolean reachable;

        if (!connectionQueryRequest.getUrlConnection().contains(":")) {
            reachable = InetAddress.getByName(connectionQueryRequest.getUrlConnection()).isReachable(2000);
        } else {
            String[] split = connectionQueryRequest.getUrlConnection().split(":");
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(split[0], Integer.parseInt(split[1])), 2000);
                reachable = true;
            } catch (IOException e) {
                reachable = false;
            }
        }

        String result = "FALSE";
        if (reachable) {
            result = "TRUE";
        }

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    @PostMapping(
            value = "/deleteConnection",
            headers = {"Content-Type=text/plain;charset=UTF-8"},
            produces = Produces.APPLICATION_JSON_UTF8)
    public ResponseEntity<String> deleteConnection(@RequestBody String body) throws IOException {
        ConnectionQueryRequest connectionQueryRequest = gson.fromJson(body, ConnectionQueryRequest.class);

        ConnectionConfig.deleteConnection(connectionQueryRequest.getNameConnection());

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body("");
    }
}
