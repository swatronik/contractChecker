package src.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.ArrayList;

@Data
public class CreateQueryRequest {

    @SerializedName("name")
    private String name;

    @SerializedName("url")
    private String url;

    @SerializedName("method")
    private String method;

    @SerializedName("param")
    private String param;

    @SerializedName("connections")
    private ArrayList<String> connections;

    @SerializedName("folderName")
    private String folderName;

    @SerializedName("bodyText")
    private String bodyText;

    @SerializedName("responseText")
    private String responseText;
}
