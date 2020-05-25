package com.serenitybdd.cucumber.steps;

import com.serenitybdd.cucumber.steps.serenity.LoginSteps;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.Steps;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;

public class GoogleSearchSteps {

    @Steps
    LoginSteps loginsteps;

    @Managed
    private WebDriver driver;


    @Given("open google")
    public void openGoogle(){
        driver.navigate().to("https://google.com");

    }

    @When("we search for text")
    public void we_search_for_text_searchText(io.cucumber.datatable.DataTable dataTable) {
      List<String> textToSearch = dataTable.asList(String.class);
      for(String s: textToSearch){
          loginsteps.typeInSearch(s);
      }


    }
    @When("we search for text (.*)$")
    public void we_search_for_text_searchText(String searchText) {
        loginsteps.typeInSearch(searchText);
    }

    @Then("we can see text (.*)$")
    public void we_can_see_text_sudy(String valueToFind) {
      loginsteps.searchStepsShouldHave(valueToFind);
    }



}
