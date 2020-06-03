package com.serenitybdd.cucumber.steps.serenity;

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
			else
				res.then().assertThat().body(xPathForResponse, Matchers.equalTo(responseString));
	    }
	 
	 @Step("Verfied the Database details Expected value {0} for column {2} \n"
	 		+  "Query Ran :\n {3} \n"
			 + "Actual Value:{1} \n"
			 + "Expected Value: {0}")
	    public void checkDBValidation(String expected , String actual, String dbColumnName , String query){
		 Assert.assertEquals(expected, actual);
	    }

}
