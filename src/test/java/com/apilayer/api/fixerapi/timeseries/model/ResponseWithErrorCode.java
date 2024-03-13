package com.apilayer.api.fixerapi.timeseries.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ResponseWithErrorCode {

    @JsonProperty("message")
    private String errorMessage;

    @JsonProperty("error")
    private Error error;

    @Getter
    public static class Error {
        @JsonProperty("code")
        private Integer code;

        @JsonProperty("type")
        private String type;

        @JsonProperty("info")
        private String info;
    }
}
