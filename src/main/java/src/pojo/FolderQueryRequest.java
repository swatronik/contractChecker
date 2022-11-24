package src.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class FolderQueryRequest {

    @SerializedName("nameFolder")
    private String nameFolder;

    @SerializedName("nameQuery")
    private String nameQuery;
}
