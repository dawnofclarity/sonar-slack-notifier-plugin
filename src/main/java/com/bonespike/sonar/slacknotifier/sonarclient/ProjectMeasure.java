package com.bonespike.sonar.slacknotifier.sonarclient;

import lombok.Data;

@Data
public class ProjectMeasure {
    private String metric;
    private String value;
    private String lastValue;


    public Long asNumberValue(){
        try {
            return Long.parseLong(value);
        }catch(Exception e){
            return 0L;
        }
    }
    public Long lastAsNumberValue(){
        try {
            return Long.parseLong(lastValue);
        }catch(Exception e){
            return 0L;
        }
    }
    public Long getDiff(){
        return  asNumberValue() - lastAsNumberValue() ;
    }
}
