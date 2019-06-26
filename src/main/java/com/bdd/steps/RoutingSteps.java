package com.bdd.steps;

import cucumber.api.java8.En;
import org.springframework.beans.factory.annotation.Value;

public class RoutingSteps extends BaseSteps implements En {

    @Value("${channel.routing.endpoint}")
    private String endpoint;


}
