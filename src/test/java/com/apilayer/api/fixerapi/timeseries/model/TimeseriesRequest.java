package com.apilayer.api.fixerapi.timeseries.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TimeseriesRequest {

  private String startDate;

  private String endDate;

  private String base;

  private String symbols;
}
