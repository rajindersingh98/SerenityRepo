package com.serenitybdd.pages;

import net.serenitybdd.core.annotations.findby.FindBy;
import net.serenitybdd.core.pages.PageObject;
import net.serenitybdd.core.pages.WebElementFacade;
import net.thucydides.core.annotations.Step;

import javax.servlet.annotation.WebFilter;


public class LoginPage extends PageObject {

    @FindBy(css ="[title='Search']")
    private WebElementFacade searchText;

    @FindBy(css ="[role='listbox']")
    private WebElementFacade searchResults;


    @Step("search for {0}")
    public LoginPage typeInSearch(String a){
         searchText.waitUntilPresent();
         searchText.type(a);
         return switchToPage(LoginPage.class);
    }

    public void searchStepsShouldHave(String a){
        searchResults.waitUntilPresent();
        searchResults.shouldContainText(a);
    }
}
