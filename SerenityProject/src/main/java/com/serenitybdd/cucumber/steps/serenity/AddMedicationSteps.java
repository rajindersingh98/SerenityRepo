package com.serenitybdd.cucumber.steps.serenity;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.matcher.ResponseAwareMatcher;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import junit.framework.Assert;
import net.serenitybdd.rest.SerenityRest;
import net.thucydides.core.annotations.Step;

public class AddMedicationSteps {
	//Response res = null;
	
	@Step("Send Patient ExternalID Set Scenarios")
    public void scenario(){
	
	}
	
	 @Step("Submit Send Patient API as below - {1}.")
	    public Response submitRequest(String request, String testcaseName){
		 Response res = SerenityRest.given().contentType(ContentType.XML).accept(ContentType.XML).body(request).post("http://10.100.12.154:51001/servlet/rcopia.servlet.EngineServlet");
		 return res;
	 }
	 
	 @Step("Verified {1} is present in response at path {2}")
	    public void checkResponse(Response res, String responseString , String xPathForResponse){
		 //List<String> lmSet = XmlPath.from(res.asString()).getList("RCExtResponse.Response.PatientList.Patient.ExternalIdentifierList");
		// System.out.println("lmSetlmSetlmSetlmSet"+lmSet);
		 List<String> lm = XmlPath.from(res.asString()).getList(xPathForResponse);
			if(lm.size() > 1)
				res.then().assertThat().body(xPathForResponse, Matchers.hasItems(responseString.split(",")));
			else {
				if("blank".equals(responseString.trim())) {
					res.then().assertThat().body(xPathForResponse, Matchers.equalTo(""));	
				}
				else {
					res.then().assertThat().body(xPathForResponse, Matchers.equalTo(responseString.trim()));
				}
			}
	    }
	 
	 @Step("DB Validation Expected value {1}")
	    public void checkDBValidation(ArrayList listOfResultsFromDB, String expectedValue){
		     assertTrue(listOfResultsFromDB.contains(expectedValue));
	    }

}
