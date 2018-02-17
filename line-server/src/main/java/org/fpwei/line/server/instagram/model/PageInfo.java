package org.fpwei.line.server.instagram.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageInfo {
    @JsonProperty("has_next_page")
    private boolean hasNextPage;

    @JsonProperty("end_cursor")
    private String endCursor;
}
