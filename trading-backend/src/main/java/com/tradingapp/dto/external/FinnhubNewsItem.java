package com.tradingapp.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinnhubNewsItem {

    private String headline;
    private String source;
    private String url;
    private String summary;
    private String image;
    private long datetime;
}
