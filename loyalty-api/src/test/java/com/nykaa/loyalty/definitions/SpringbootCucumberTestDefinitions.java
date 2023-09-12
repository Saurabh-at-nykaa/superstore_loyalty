package com.nykaa.loyalty.definitions;

import com.nykaa.loyalty.utils.BuildingTestData;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.given;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpringbootCucumberTestDefinitions {

    private String key = "";

    @When("cucumber calls to build test data with caseNo {string} and {string} from test file")
    public void buildTestData(String index,String testOutcome){
        key = index+ "_" +testOutcome;
    }

    @Given("the client calls fetchEdd")
    public void sendRequest() throws Throwable {
        String urlEndpoint = "fetchEDD";
        String inputData = BuildingTestData.buildTestDataForSuperstoreAggregator(key,urlEndpoint);
        given().body(inputData).contentType("application/json").
                when().post("http://localhost:8080/edd/product/edd/allocate/fetch").
                then().statusCode(200);
    }


    @Then("the user will upload new edd")
    public void testUpload() throws Throwable{
        String urlEndpoint = "singleUpload";
        String inputData = BuildingTestData.buildTestDataForSuperstoreAggregator(key,urlEndpoint);
        given().body(inputData).contentType("application/json").
                when().post("http://localhost:8080/edd/input/singleUpload").
                then().statusCode(200);
    }



}