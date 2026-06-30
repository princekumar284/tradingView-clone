package com.tradingapp.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NewsArticleResponse {

    private String headline;
    private String source;
    private String url;
    private String summary;
    private String image;
    private long datetime;
}
