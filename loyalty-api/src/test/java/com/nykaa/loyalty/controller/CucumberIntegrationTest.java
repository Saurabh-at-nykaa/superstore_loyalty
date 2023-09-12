package com.nykaa.loyalty.controller;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty","html:target/cucumber-report/cucumber.html","json:target/cucumber-report/cucumber.json"},
        features = {"src/test/resources/CucumberTest.feature"},
        glue = { "com.nykaa.superstore.aggregator.definitions"})
public class CucumberIntegrationTest {
}