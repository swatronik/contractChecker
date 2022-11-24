package src.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class CreateFolderQueryRequest {

    @SerializedName("nameFolder")
    private String nameFolder;

    @SerializedName("url")
    private String url;

    @SerializedName("method")
    private String method;

    @SerializedName("nameToken")
    private String nameToken;

    @SerializedName("connection")
    private String connection;

    @SerializedName("bodyText")
    private String bodyText;

    @SerializedName("responseText")
    private String responseText;
}