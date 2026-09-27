package io.paideia.content.service.bdd;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectDirectories;
import org.junit.platform.suite.api.Suite;

@Suite
@IncludeEngines("cucumber")
@SelectDirectories("../../features/content")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "io.paideia.content.service.bdd")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty,summary")
class ContentCucumberTest {
}
