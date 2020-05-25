package com.serenitybdd;

import com.serenitybdd.cucumber.steps.serenity.LoginSteps;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.Steps;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;

@RunWith(SerenityRunner.class)
public class MyFirstSerenity
{
    @Managed
    private WebDriver driver;

    @Steps
    LoginSteps loginSteps;

    @Test
    public void myFirstTest(){
        driver.navigate().to("https://google.com");
        loginSteps.typeInSearch("gola");
        loginSteps.searchStepsShouldHave("ffff");
    }
}
