package com.serenitybdd.steps;

import com.serenitybdd.pages.LoginPage;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.steps.ScenarioSteps;

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
