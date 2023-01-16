package com.bonespike.sonar.slacknotifier.sonarclient;

import lombok.Data;

import java.util.List;

@Data
public class GetMeasureResponse {
    private Component component;
}

@Data
class Component {
    String key;
    String name;
    String qualifier;
    List<ProjectMeasure> measures;
}
