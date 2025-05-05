package codepirate.tubelensbe.funnel.dto;

import lombok.Data;
import java.util.List;

@Data
public class AnalyticsReportResponse {
    private String kind;
    private List<ColumnHeader> columnHeaders;
    private List<List<Object>> rows;

    @Data
    public static class ColumnHeader {
        private String name;
        private String columnType;
        private String dataType;
    }
}

