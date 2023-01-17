package com.bonespike.sonar.slacknotifier.extension.task;

import com.bonespike.sonar.slacknotifier.common.component.ProjectConfig;
import com.bonespike.sonar.slacknotifier.sonarclient.MeasureHistoryDetails;
import com.bonespike.sonar.slacknotifier.sonarclient.ProjectMeasure;
import com.bonespike.sonar.slacknotifier.sonarclient.SonarClient;
import com.github.seratch.jslack.api.webhook.Payload;
import com.bonespike.sonar.slacknotifier.common.component.AbstractSlackNotifyingComponent;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.util.VisibleForTesting;
import org.sonar.api.ce.posttask.Branch;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.config.Configuration;
// import org.sonar.api.i18n.I18n;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by 616286 on 3.6.2016.
 * Modified by gmilosavljevic
 */
public class SlackPostProjectAnalysisTask extends AbstractSlackNotifyingComponent implements PostProjectAnalysisTask {

    private static final Logger LOG = Loggers.get(SlackPostProjectAnalysisTask.class);
    private List<String> defaultMetrics = Arrays.asList("ncloc","development_cost","sqale_index","vulnerabilities","security_rating","security_review_rating");
    private final SlackHttpClient httpClient;

    @VisibleForTesting
    SlackPostProjectAnalysisTask(final SlackHttpClient httpClient, final Configuration settings
    // ,final I18n i18n
    ) {
        super(settings);
        LOG.info("======================SLACK===============");
    //    this.i18n = i18n;
        this.httpClient = httpClient;
    }

    /**
     * Default constructor invoked by SonarQube.
     *
     * @param settings
     */
    public SlackPostProjectAnalysisTask(final Configuration settings
    //    , final I18n i18n
    ) {
        super(settings);
        LOG.info("======================SLACK===============");
//        this.i18n = i18n;
        httpClient = new SlackHttpClient(settings);

    }

    public String getDescription() {
        return "Sonar Plugin to offer Slack Notifications globally or per-project";
    }

    @Override
    public void finished(PostProjectAnalysisTask.Context context) {

        final ProjectAnalysis analysis = context.getProjectAnalysis();
        this.refreshSettings();
        if (!this.isPluginEnabled()) {
            LOG.info("Slack notifier plugin disabled, skipping. Settings are [{}]", this.logRelevantSettings());
            return;
        }
        LOG.info("Analysis ScannerContext: [{}]", analysis.getScannerContext().getProperties());
        final String projectKey = analysis.getProject().getKey();

        LOG.info("Looking for the configuration of the project {}", projectKey);
        final Optional<ProjectConfig> projectConfigOptional = this.getProjectConfig(projectKey);
        if (!projectConfigOptional.isPresent()) {
            return;
        }
        LOG.info("getting project metrics");
        // final List<ProjectMeasure> metrics = S


        // final var projectConfig =
        ProjectConfig projectConfig = projectConfigOptional.get();
        String targetBranch = getTargetBranch(projectConfig);
        String builtBranch = context.getProjectAnalysis().getBranch().map(b -> b.getName()).orElse(Optional.of("")).get();

        LOG.info("Project Key : {}", projectConfig.getProjectKey());
        LOG.info("targetBranch- {}  / builtBranch {}", targetBranch, builtBranch);
        if(StringUtils.isNotBlank(targetBranch) && !StringUtils.equals(targetBranch, builtBranch)) {
            LOG.info("Branch doesn match, returing ...");
            return;
        }

        LOG.info("Is moving forward");
        if (this.shouldSkipSendingNotification(projectConfig, analysis.getQualityGate())) {
            return;
        }

        final String hook = this.getSlackHook(projectConfig);
        Map lookupMap = new HashMap<String,String>();
        LOG.info("=====================================");
        try {
            SonarClient sc = new SonarClient(getServerToken(), getSonarServerUrl());
            List<ProjectMeasure> latestMeasures = sc.getMeasures(projectConfig.getProjectKey(), defaultMetrics);


            for (ProjectMeasure m : latestMeasures){
                LOG.info("adding " + m.getMetric() + "with value " + m.getValue());
                lookupMap.put("analysis."+m.getMetric(),m.getValue());
                lookupMap.put("analysis.prior."+m.getMetric(),m.getLastValue());
                lookupMap.put("analysis.diff."+ m.getMetric(),m.getDiff());
            }
        } catch (InterruptedException|IOException|URISyntaxException e) {
            LOG.error("problem with getting stats",e);
            e.printStackTrace();
        }
        LOG.info("Slack notification will be sent: {}", analysis.toString());
        Optional<Branch> branch = analysis.getBranch();
        String branchName = branch.get().getName().orElse("");
        lookupMap.put("project.url",this.projectUrl(projectKey) + (branch.isPresent() ? "&branch=" + branchName : ""));
        lookupMap.put("project.name",analysis.getProject().getName());
        lookupMap.put("analysis.targetBranch",targetBranch);
        lookupMap.put("analysis.branch", branchName);
        lookupMap.put("analysis.buildBranch",builtBranch);

        //final var payload =
        Payload payload = ProjectAnalysisPayloadBuilder.of(analysis, this.getSlackTemplate())
            // .i18n(this.i18n)
            .projectConfig(projectConfig)
            .projectUrl(this.projectUrl(projectKey))
            .props(lookupMap)
            .includeGate(this.getIncludeGate())
            .includeBranch(this.isBranchEnabled())
            .username(this.getSlackUser())
            .iconUrl(this.getIconUrl())
            .build();

        try {

            if (httpClient.invokeSlackIncomingWebhook(hook, payload)) {
                LOG.info("Slack webhook invoked with success.");
            } else {
                throw new IllegalArgumentException("The Slack response has failed");
            }
        } catch (final IOException e) {
            LOG.error("Failed to send slack message {}", e.getMessage(), e);
        }
    }
    private String getTargetBranch(ProjectConfig projectConfig) {
        String[] split = projectConfig.getProjectKey().split(";");
        if (split.length == 2) {
            return split[1];
        }
        return "";
    }
    private String getSlackHook(final ProjectConfig projectConfig) {
        String hook = projectConfig.getProjectHook();
        if (hook != null) {
            hook = hook.trim();
        }
        LOG.info("Hook is: {}", hook);
        if (hook == null || hook.isEmpty()) {
            hook = this.getDefaultHook();
        }
        return hook;
    }

    private String projectUrl(final String projectKey) {
        return this.getSonarServerUrl() + "dashboard?id=" + projectKey;
    }
}
