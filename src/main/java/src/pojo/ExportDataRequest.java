package src.pojo;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.ArrayList;

@Data
public class ExportDataRequest {

    @SerializedName("runPeriod")
    private String runPeriod;

    @SerializedName("connections")
    private ArrayList<ConnectionQueryRequest> connections;

    @SerializedName("folders")
    private ArrayList<CreateFolderQueryRequest> folders;

    @SerializedName("queries")
    private ArrayList<CreateQueryRequest> queries;
}
