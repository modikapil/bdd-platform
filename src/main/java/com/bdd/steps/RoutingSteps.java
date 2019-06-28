package com.bdd.steps;

import cucumber.api.java8.En;
import org.springframework.beans.factory.annotation.Value;
import com.google.common.collect.ImmutableMap;
import com.rbsgrp.journeymanager.PlatformRegistry;
import com.rbsgrp.routing.gateway.response.login.LoginResponse;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.api.java8.En;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RoutingSteps extends BaseSteps implements En {

    @Value("${channel.routing.endpoint}")
    private String endpoint;

    private String bin;
    private String dbid;
    private String iam_subject;
    private ResponseEntity responseEntity;

    @Given("^a valid bin \"([^\"]*)\", dbid \"([^\"]*)\", iam_subject \"([^\"]*)\"$")
    public void aValidBinDbidIam_subject(String bin, String dbid, String iam_subject) throws Throwable {
        this.bin = bin;
        this.dbid = dbid;
        this.iam_subject = iam_subject;
    }

    @And("^a valid routing request$")
    public void aValidRoutingRequest() throws Throwable {
        urlToTest = channelHost + routingUrl;
    }

    @When("^routing api is called$")
    public void routingApiIsCalled() throws Throwable {
        final Map<String, String> headerValues = ImmutableMap.of(
                "iam_r4p_bin", bin,
                "iam_r4p_dbid", dbid,
                "iam_subject", iam_subject);

        responseEntity = executeGet(urlToTest, buildHeaders(headerValues), String.class, "LOGIN");
    }

    @Then("^a successful routing response is returned$")
    public void aSuccessfulResponseIsReturned() throws Throwable {
        Assert.assertTrue(responseEntity.getStatusCode() == HttpStatus.OK);
    }

    @And("^the response contains tier (\\d+)$")
    public void theResponseContainsTier(int arg0) throws Throwable {
        ResponseEntity loginResponseEntity = (ResponseEntity) PlatformRegistry.getInstance().getValue("LOGIN");

        LoginResponse loginResponse = (LoginResponse) mapToResponseEntity(loginResponseEntity, LoginResponse.class).getBody();
        Assert.assertTrue(loginResponse.getTier().getCustomerTier().equals("TIER_4"));
    }

}
