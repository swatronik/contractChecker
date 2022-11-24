package src.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class ConnectionQueryRequest {

    @SerializedName("nameConnection")
    private String nameConnection;

    @SerializedName("urlConnection")
    private String urlConnection;
}
