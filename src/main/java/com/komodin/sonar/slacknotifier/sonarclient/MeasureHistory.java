package com.komodin.sonar.slacknotifier.sonarclient;

import lombok.Data;

import java.util.List;

@Data
public class MeasureHistory {
    private Paging paging;
    private List<MeasureHistoryDetails> measures;
}
@Data
class Paging {
    private Integer pageIndex;
    private Integer pageSize;
    private Integer total;
}

