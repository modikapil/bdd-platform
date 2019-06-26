package com.bdd.runners;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = {"classpath:features"}, plugin = {"pretty", "html:output"}, glue = {"com.bdd"})
public class AppRunnerTest {
}
