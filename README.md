# Disclaimer

This project is a fork of the original work from https://github.com/sleroy/sonar-slack-notifier-plugin and https://github.com/bonespike/sonar-slack-notifier-plugin.

Together with @listemanuel95, we made modifications to have internationalization taking in count the changes introduced in the latest versions of SonarQube.

Starting from version 9.5 they separated the API from the core of Sonar, so it is now in a separate repository and they removed the i18n methods.
We've tried to use `org.sonar.api.utils.LocalizedMessages` but the `core.properties` localization file is part of the core and is not accesible from plugins.

At last, we're sorry for repackaging the code under our own domain. We don't plant to merge it back to sleroy's nor bonespike's repos. Just appreciation words for the work they did.

## Pre-requisites

Built with JDK 17.0.13 & Maven 3.8.6.

# Works with

Tested with SonarQube 10.7.

# Sonar Slack Notifier Plugin

SonarQube plugin for sending notifications to Slack

This plugin sends a Slack message of project analysis outcome to configured project specific slack channel.
The plugin uses Incoming Web Hook as the integration mechanism with Slack and Slack-compatible API's.

# Install

The plugin must be placed in *SONAR_HOME/extensions/plugins* directory and SonarQube must be restarted.

## Using latest release

You can find the latest release from https://github.com/komodin/sonar-slack-notifier-plugin/releases/ page.
Download the latest verion's jar file and drop in the directory mentioned above

## Building from source

To build the plugin simply run:
```
mvn clean package
```

And it you will get the built .jar inside `target` directory.

# Configuration (updates needed)

After the plugin has been installed, you need to configure it.
Although SonarQube offers project level configurations for some plugins, they cannot be used with this plugin because it runs in the "server side", and only sees the global settings.

First of all, as administrator, go to Administration > Security > Users:

![](documentation/screenshots/administration_security_users.png?raw=true)

Click on Admin user tokens burger button:

![](documentation/screenshots/administration_tokens_button.png?raw=true)

Generate a new token with "No expiration" and copy it:

![](documentation/screenshots/administration_new_token.png?raw=true)

Go to Administration > Configuration > General Settings and you will see a new `Slack` category in the sidebar:

![](documentation/screenshots/administration_sidebar.png?raw=true)

There are some mandatory fields that you have to fulfill:
- Slack user alias
- Plugin enabled
- Default Slack channel
- Server Token

In `Server Token` you have to paste the previously generated token and then click on `Save` button.

There's two ways to configure this plugin: in a generic way or in a per-project basis.

If you want to configure it in a generic way, you have to fulfill the `Slack web integration hook`:

![](documentation/screenshots/administration_slack_part1.png?raw=true)

And also the `Default Slack channel`:

![](documentation/screenshots/administration_slack_part2.png?raw=true)

And to configure it in a per-project basis, you have to configure the `Project Hook` URL, the `Project Key` (regex that will match the Project Key of the project) and the channel where you want to send the notifications:

![](documentation/screenshots/administration_slack_part3.png?raw=true)

Note: if you want to the notification to be sent either if the Quality Gate passed ok or if it failed, you have to click twice on the `Send only if Quality Gate failed` (note that by default is says "(not set)").

## Wildcard support

The project key supports wildcards at the end. See https://github.com/kogitant/sonar-slack-notifier-plugin/issues/2
 
# Example messages posted to Slack
## New bug introduced

![](documentation/screenshots/example_slack_message_red_due_to_new_bug.png)

## All good

![](documentation/screenshots/example_slack_message_all_green.png)

# Inspired by

* https://github.com/sleroy/sonar-slack-notifier-plugin
* https://github.com/bonespike/sonar-slack-notifier-plugin
* https://github.com/astrebel/sonar-slack-notifier-plugin
* https://github.com/dbac2002/sonar-hipchat-plugin

# Benefits from

* https://github.com/seratch/jslack

# SonarQube Plugin Development guides

* https://docs.sonarqube.org/latest/extension-guide/developing-a-plugin/plugin-basics/

# Slack-compatible webhook integration and message formatting guides

 * https://api.slack.com/custom-integrations
 * https://api.slack.com/docs/attachments#message_formatting
 * https://api.slack.com/docs/attachments
 * https://developers.mattermost.com/integrate/webhooks/incoming/
 * https://docs.mattermost.com/channels/format-messages.html

# Analyzing this project with unit test and integration test coverage

```
    mvn clean jacoco:prepare-agent install -DskipITs=true
    mvn jacoco:prepare-agent-integration failsafe:integration-test
    mvn sonar-maven-plugin:sonar -Dsonar.host.url=http://localhost:9000
```
