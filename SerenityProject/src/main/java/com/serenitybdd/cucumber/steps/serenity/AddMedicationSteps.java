package com.serenitybdd.cucumber.steps.serenity;

import org.hamcrest.Matchers;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import junit.framework.Assert;
import net.serenitybdd.rest.SerenityRest;
import net.thucydides.core.annotations.Step;

public class AddMedicationSteps {
	Response res = null;
	
	@Step("Send Patient ExternalID Set Scenarios")
    public void scenario(){
	
	}
	
	 @Step("Submit Send Patient API as below - {1}.")
	    public Response submitRequest(String request, String testcaseName){
		 res = SerenityRest.given().contentType(ContentType.XML).accept(ContentType.XML).body(request).post("http://10.100.12.154:51001/servlet/rcopia.servlet.EngineServlet");
		 return res;
	 }
	 
	 @Step("Verified {0} is present in response at path {1}")
	    public void checkResponse(String responseString , String xPathForResponse){
		 res.then().assertThat().body(xPathForResponse, Matchers.is(responseString));
	    }
	 
	 @Step("Verfied the Database details Expected value {0} for column {2} \n"
	 		+  "Query Ran :\n {3} \n"
			 + "Actual Value:{1} \n"
			 + "Expected Value: {0}")
	    public void checkDBValidation(String expected , String actual, String dbColumnName , String query){
		 Assert.assertEquals(expected, actual);
	    }

}
