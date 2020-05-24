package com.serenitybdd;

import com.serenitybdd.pages.LoginPage;
import com.serenitybdd.steps.LoginSteps;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.thucydides.core.annotations.Managed;
import net.thucydides.core.annotations.Steps;
import org.junit.Assert;
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
