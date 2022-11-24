package src.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class UpdateQueryRequest {

    @SerializedName("nameFolder")
    private String nameFolder;

    @SerializedName("nameQuery")
    private String nameQuery;

    @SerializedName("updateObject")
    private CreateQueryRequest updateObject;
}
