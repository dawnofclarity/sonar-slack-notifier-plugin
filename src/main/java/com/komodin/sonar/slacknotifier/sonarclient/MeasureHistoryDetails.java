package com.komodin.sonar.slacknotifier.sonarclient;

import lombok.Data;

import java.util.List;

@Data
public class MeasureHistoryDetails {
    private String metric;
    private List<MetricRecord> history;
}
@Data
class MetricRecord {
    private String date;
    private String value;
}
