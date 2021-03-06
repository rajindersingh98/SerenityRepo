package com.serenitybdd.cucumber.steps.serenity;




import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import junit.framework.Assert;
import net.serenitybdd.rest.SerenityRest;
import net.thucydides.core.annotations.Step;

public class AddPracticeSteps {
	 
	Response res = null;
	
	 @Step("I submit the Request for {1}")
	    public Response submitRequest(String request, String testcaseName){
		 res = SerenityRest.given().contentType(ContentType.XML).accept(ContentType.XML).body(request).post("https://register.qa.drfirst.com/ws/wsAddPractice");
		 return res;
	 }
	 
	 @Step("Then Response has {0} Code/Text for path {1}")
	    public void checkResponse(String responseString , String xPathForResponse){
		 res.then().assertThat().body(xPathForResponse, Matchers.is(responseString));
	    }

	 @Step("DB Validation column value Expected {0} , Actual {1} for DB column name {2} for query {3}")
	    public void checkDBValidation(String expected , String actual, String dbColumnName , String query){
		 Assert.assertEquals(expected, actual);
	    }
}
