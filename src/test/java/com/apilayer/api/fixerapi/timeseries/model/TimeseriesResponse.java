package com.apilayer.api.fixerapi.timeseries.model;

import java.time.LocalDate;
import java.util.Map;

import com.apilayer.api.fixerapi.symbols.FixerCurrency;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class TimeseriesResponse extends ResponseWithErrorCode {

  @JsonProperty("success")
  private Boolean success;

  @JsonProperty("timeseries")
  private Boolean timeseries;

  @JsonProperty("start_date")
  private LocalDate startDate;

  @JsonProperty("end_date")
  private LocalDate endDate;

  @JsonProperty("base")
  private FixerCurrency base;

  @JsonProperty("rates")
  private Map<LocalDate, Map<FixerCurrency, Double>> rates;
}
