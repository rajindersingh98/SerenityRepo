package com.serenitybdd.cucumber.steps.serenity;

import com.serenitybdd.pages.LoginPage;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.steps.ScenarioSteps;
import org.openqa.selenium.WebDriver;

public class LoginSteps extends ScenarioSteps {
    LoginPage loginPage;

    @Step("search for {0}")
    public LoginPage typeInSearch(String a){
        return loginPage.typeInSearch(a);
    }

    @Step("Then the search shoulhave text {0} ")
    public void searchStepsShouldHave(String a ) {
        loginPage.searchStepsShouldHave(a);
    }
}
