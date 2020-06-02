package com.serenitybdd;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.db.DFDBConnection;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.serenitybdd.cucumber.steps.serenity.AddMedicationSteps;


import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import net.serenitybdd.core.Serenity;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;

import net.thucydides.core.annotations.Steps;
import net.thucydides.junit.annotations.Qualifier;
import net.thucydides.junit.annotations.TestData;

@RunWith(SerenityParameterizedRunner.class)

public class sendPatientAPI {

	static int testcase = 0;

	@Steps
	AddMedicationSteps addMedicationSteps;

	@TestData(columnNames = "TestcaseID,Steps")
	public static Collection<Object[]> testData() throws IOException {
		List<Object[]> results = new ArrayList<Object[]>();
		@SuppressWarnings("unchecked")
		ArrayList<Map<String, String>> testDataMap = readCSVAndReturnMap();
		List<List<Map<String, String>>> result = testDataMap.stream()
				.collect(Collectors.groupingBy(m -> m.get("Group"))).entrySet().stream()
				.sorted(Map.Entry.comparingByKey()) // sort on requirement
				.map(java.util.Map.Entry::getValue).collect(Collectors.toList());

		listOfTestcases = result;

		for (int i = 0; i < result.size(); i++) {
			Map<String, String> firstParameter = result.get(i).get(0);
			results.add(new Object[] { firstParameter.get("Group") ,firstParameter.get("Scenario")  });
		}
		return results;
	}

	@Qualifier
	public String qualifier() {
		return testCaseName;
	}

	private static List<List<Map<String, String>>> listOfTestcases;
	String testCaseName;
	private String randomName = null;
	String request;

	public sendPatientAPI(String name, String scenarioName) {
		this.testCaseName = name;
	}

	@Test
	public void sendPatientExternalIDSetTests() throws IOException, SQLException {
		String responseAsString  = null;
		List<Map<String, String>> numberOfStepsInGroup = listOfTestcases.get(testcase);
		for (int i = 0; i < numberOfStepsInGroup.size(); i++) {
			@SuppressWarnings("rawtypes")
			Map m = numberOfStepsInGroup.get(i);
			if(!m.get("GetDataFromPreviousTest").equals("")) {
				String dataNeededFromPreviousTest[] = m.get("GetDataFromPreviousTest").toString().split("=");
				String dataKey = dataNeededFromPreviousTest[0];
				String dataValue = XmlPath.from(responseAsString).get(dataNeededFromPreviousTest[1]);
				m.put(dataKey, dataValue);
			}
			if(m.get("Test Case ID").equals("Scenario Defination")) {
				addMedicationSteps.scenario();
			}
			else {
				if(m.get("Test Case ID").equals("WITHOUT_EXTERNAL_ID")) {
					System.out.println("HI");
				}
			request = processTemplate(m);
			Response r = addMedicationSteps.submitRequest(request, m.get("Test Case ID").toString());
			responseAsString = r.asString();
			
			/////// Xpath Verification in Response:
			
			String verificationMap[] = m.get("Status").toString().split(":");
			for(int indexVerificationMap = 0; indexVerificationMap < verificationMap.length ; indexVerificationMap++) {
				addMedicationSteps.checkResponse(r, verificationMap[indexVerificationMap].split("=")[1], verificationMap[indexVerificationMap].split("=")[0]);
			
			}
			

			///////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////
			// DB Validation
			if ("y".equals(m.get("DBVALIDATIONNEEDED"))) {
				String dBValidation[] = m.get("DB").toString().split("SEPERATOR");
				String queries[] = m.get("QUERY").toString().split("SEPERATOR");
				String parameters[] = m.get("PARAMETERS").toString().split("SEPERATOR");
				String verification[] = m.get("VERIFICATION").toString().split("SEPERATOR");
				for (int indexdBValidation = 0; indexdBValidation < dBValidation.length; indexdBValidation++) {
					String finalQuery = finalQuery(responseAsString, queries[indexdBValidation].trim(),
							parameters[indexdBValidation].trim());
					String dbColumnValue[] = verification[indexdBValidation].split("VERIFICATIONSEP");
					for (int indexDbColumnValue = 0; indexDbColumnValue < dbColumnValue.length; indexDbColumnValue++) {
						@SuppressWarnings("rawtypes")
						Map columnAndType = getColumnNameAndType(dbColumnValue[indexDbColumnValue]);
						String columnToVerify = getColumnValueToverify(dbColumnValue[indexDbColumnValue]);
						try {
							ResultSet rs = getResultSet(dBValidation[indexdBValidation].trim(), finalQuery.trim());
							Serenity.recordReportData().withTitle("Database evidence").andContents(rs.toString());
							String valuetoAssert = m.get(columnToVerify.trim()).toString();
							if ("RandomName".equals(m.get(columnToVerify.trim()).toString())) {
								valuetoAssert = randomName;
							}
							while (rs.next()) {
								if ("STRING".equals(columnAndType.get("type"))) {
									addMedicationSteps.checkDBValidation(valuetoAssert,
											rs.getString(columnAndType.get("column").toString()),
											columnAndType.get("column").toString(), finalQuery.trim());
								}
							}
						} finally {
							DFDBConnection.closeConnection();
						}
					}

					// Run query and validate verifications
				}
			}

			///////////////////////////////////////////////////////////////////////////
		  }
		}
		
	}

	@After
	public void tearDown() {
		testcase++;
	}
	public String getColumnValueToverify(String dbColumnValue) {

		return dbColumnValue.split("=")[1].trim();
	}

	public Map getColumnNameAndType(String dbColumnValue) {
		Map<String, String> columnAndType = new HashMap<>();
		String columnNameAndType = dbColumnValue.split("=")[0].trim();
		String column = columnNameAndType.split("TYPE")[0].trim();
		String type = columnNameAndType.split("TYPE")[1].trim();
		columnAndType.put("column", column);
		columnAndType.put("type", type);
		return columnAndType;

	}

	public String finalQuery(String response, String query, String parameters) {
		String parametersArray[] = parameters.split("PARAMETERSEP");
		for (int indexParametersArray = 0; indexParametersArray < parametersArray.length; indexParametersArray++) {
			String parameterAndValueArray[] = parametersArray[indexParametersArray].split("=");
			String parameter = parameterAndValueArray[0].trim();
			String value = XmlPath.from(response).get(parameterAndValueArray[1].trim());
			query = query.replace(parameter, value);
		}
		return query;
	}

	@SuppressWarnings({ "rawtypes", "unchecked"})
	public Map getXmlVerificationMap(String[] xmlVerification) {
		Map h = new HashMap();
		for (int index = 0; index < xmlVerification.length; index++) {
			String[] keyvalue = xmlVerification[index].split("=");
			h.put(keyvalue[0], keyvalue[1]);
		}
		return h;
	}
	
	

	public String processTemplate(@SuppressWarnings("rawtypes") Map templateData) throws IOException {
		Configuration cfg = new Configuration(new Version("2.3.23"));
		cfg.setDefaultEncoding("UTF-8");
		cfg.setDirectoryForTemplateLoading(new File("src\\test\\resources\\restassuredrequests"));
		Template t = cfg.getTemplate("send_patient.xml");
		StringWriter out = new StringWriter();
		try {
			t.process(templateData, out);
		} catch (TemplateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		out.flush();
		return out.toString();

	}

	@SuppressWarnings("rawtypes")
	public static ArrayList readCSVAndReturnMap() throws IOException {
		@SuppressWarnings("unused")
		Map<String, String> keyVals = null;
		ArrayList<Map> list = new ArrayList<>();
		File file = new File("src\\test\\resources\\csv\\send_patient.csv");
		Reader reader = new FileReader(file);
		Iterator<Map<String, String>> iterator = new CsvMapper().readerFor(Map.class)
				.with(CsvSchema.emptySchema().withHeader()).readValues(reader);
		while (iterator.hasNext()) {
			// keyVals = iterator.next();
			list.add(iterator.next());

		}
		return list;
	}

	public String getRandomName() {
		String letter[] = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J" };
		Date date = new Date();
		String timeString = String.valueOf(date.getTime());
		String randomString = "";
		for (int index = 0; index < timeString.length(); index++) {
			randomString = randomString + letter[Integer.parseInt(timeString.substring(index, index + 1))];
		}
		String lastName = randomString;
		return lastName;
	}

	public String finalRequest(String request) {
		request = request.replace("RandomExt", "Rohit_" + Math.random());
		randomName = getRandomName();
		request = request.replace("RandomName", randomName);
		request = request.replace("RandomName", randomName);
		request = request.replace("<LicenseCapCount>deleted</LicenseCapCount>", "");
		request = request.replace("<PhoneExt>deleted</PhoneExt>", "");
		request = request.replace("<BackPhoneExt>deleted</BackPhoneExt>", "");
		return request;
	}

	public ResultSet getResultSet(String dbName, String query) {
		Statement st = DFDBConnection.getStatement(dbName);
		ResultSet rs = null;
		try {
			rs = st.executeQuery(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return rs;
	}
	
	

}
