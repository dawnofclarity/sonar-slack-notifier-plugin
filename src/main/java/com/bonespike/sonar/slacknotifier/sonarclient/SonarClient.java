package com.bonespike.sonar.slacknotifier.sonarclient;


import com.google.gson.Gson;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class SonarClient {
    private static final Logger LOG = Loggers.get(SonarClient.class);

    private String token;
    private String baseURL;
    private HttpClient httpClient;
    public static final String MEASURE_ENDPOINT = "api/measures/component";
    public static final String MEASURE_HISTORY_ENDPOINT = "api/measures/search_history";

    private Gson gson;
    public SonarClient(String token, String baseURL){
        this.token = token;
        this.baseURL = baseURL;
        LOG.info("==== TOKEN ====");
        LOG.info(token);

        this.httpClient = HttpClient.newBuilder()
//                          .authenticator(new Authenticator() {
//                              @Override
//                              protected PasswordAuthentication getPasswordAuthentication() {
//                                  return new PasswordAuthentication(token,null);
//                              }
//                          })
                          .build();
        this.gson = new Gson();
    }
    private String getAuthValue(){
        String valueToEncode = token + ":";
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());

    }
    public List<ProjectMeasure> getMeasures(String projectKey, List<String> metrics) throws URISyntaxException, IOException, InterruptedException {
        URI targetURI = new URI(baseURL + MEASURE_HISTORY_ENDPOINT + "?component=" + projectKey + "&ps=10&metrics=" + String.join(",", metrics));
        HttpRequest req = HttpRequest.newBuilder()
            .uri(targetURI)
            .header("Authorization",getAuthValue())
            .GET()
            .build();
        HttpResponse<String> getResponse = httpClient.send(req,HttpResponse.BodyHandlers.ofString());
        LOG.info(getResponse.body());
        MeasureHistory response = gson.fromJson(getResponse.body(), MeasureHistory.class);
        List<ProjectMeasure> result = new ArrayList<>();

        for (MeasureHistoryDetails m : response.getMeasures()){
            ProjectMeasure pm = new ProjectMeasure();
            pm.setMetric(m.getMetric());
            pm.setValue(m.getHistory().get(0).getValue());
            if (m.getHistory().size()>1){
                pm.setLastValue(m.getHistory().get(1).getValue());
            }
            result.add(pm);

        }
        return result;



    }


        public List<ProjectMeasure> getLatestMeasure(String projectKey, List<String> metrics) throws URISyntaxException, IOException, InterruptedException {
        URI targetURI = new URI(baseURL + MEASURE_ENDPOINT + "?component=" + projectKey + "&metricKeys=" + String.join(",",metrics));
        LOG.info("Sending request to: " + targetURI.toString());
        HttpRequest req = HttpRequest.newBuilder()
                          .uri(targetURI)
                          .header("Authorization",getAuthValue())
                          .GET()
                          .build();
        HttpResponse<String> getResponse = httpClient.send(req,HttpResponse.BodyHandlers.ofString());

        GetMeasureResponse response = gson.fromJson(getResponse.body(), GetMeasureResponse.class);

        return response.getComponent().getMeasures();

    }
}
