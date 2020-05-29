package com.serenitybdd;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.db.DFDBConnection;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.collect.Multiset.Entry;
import com.serenitybdd.cucumber.steps.serenity.AddMedicationSteps;
import com.serenitybdd.cucumber.steps.serenity.AddPracticeSteps;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import io.restassured.path.xml.XmlPath;
import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;

import net.thucydides.core.annotations.Steps;
import net.thucydides.junit.annotations.TestData;

@RunWith(SerenityParameterizedRunner.class)
public class sendPatientAPI {

	@Steps
	AddMedicationSteps addMedicationSteps;

	@TestData
	public static Collection<Object[]> testData() throws IOException {
		List<Object[]> results = new ArrayList<Object[]>();
		ArrayList<Map<String, String>> testDataMap = readCSVAndReturnMap();
		List<List<Map<String, String>>> result = testDataMap.stream()
				.collect(Collectors.groupingBy(m -> m.get("Group"))).entrySet().stream()
				.sorted(Map.Entry.comparingByKey()) // sort on requirement
				.map(java.util.Map.Entry::getValue).collect(Collectors.toList());
		System.out.println("SIIIIIIIIIIIIIIIIIIIze" + result.size());

		for (int i = 0; i < result.size(); i++) {
			List<Map<String, String>> firstParameter = result.get(i);
			results.add(new Object[] { firstParameter });
		}
		return results;
	}

	List<Map<String, String>> listOfTescases;
	String testCaseName;
	private String randomName = null;
	String request;

	public sendPatientAPI(List<Map<String, String>> list) {
		this.listOfTescases = list;

	}

	@Test
	public void test() throws IOException, SQLException {
		for (int i = 0; i < listOfTescases.size(); i++) {
			Map m = listOfTescases.get(i);
			System.out.println(m);
			request = processTemplate(m);
			System.out.println(request);
			Response response = addMedicationSteps.submitRequest(request, m.get("Test Case ID").toString());

			/////// Xpath Verification in Response:
			@SuppressWarnings("rawtypes")
			Map xmlVerificationMap = getXmlVerificationMap(m.get("Status").toString().split(":"));
			Iterator xmlVerificationEntries = xmlVerificationMap.entrySet().iterator();

			while (xmlVerificationEntries.hasNext()) {
				Map.Entry thisEntry = (Map.Entry) xmlVerificationEntries.next();
				String key = thisEntry.getKey().toString();
				String value = thisEntry.getValue().toString();
				addMedicationSteps.checkResponse(value, key);
			}

			///////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////
			// DB Validation
			System.out.println("templateData.get(\"DBVALIDATIONNEEDED\")   " + m.get("DBVALIDATIONNEEDED"));
			if ("y".equals(m.get("DBVALIDATIONNEEDED"))) {
				String dBValidation[] = m.get("DB").toString().split("SEPERATOR");
				String queries[] = m.get("QUERY").toString().split("SEPERATOR");
				String parameters[] = m.get("PARAMETERS").toString().split("SEPERATOR");
				String verification[] = m.get("VERIFICATION").toString().split("SEPERATOR");
				for (int indexdBValidation = 0; indexdBValidation < dBValidation.length; indexdBValidation++) {
					System.out.println("dBValidation[indexdBValidation]" + dBValidation[indexdBValidation].trim());
					System.out.println("queries[indexdBValidation]" + indexdBValidation + "       "
							+ queries[indexdBValidation].trim());
					System.out.println("parameters[indexdBValidation]" + indexdBValidation + "       "
							+ parameters[indexdBValidation].trim());
					String finalQuery = finalQuery(response, queries[indexdBValidation].trim(),
							parameters[indexdBValidation].trim());
					System.out.println("finalQuery" + finalQuery);
					String dbColumnValue[] = verification[indexdBValidation].split("VERIFICATIONSEP");
					System.out.println("dbColumnValue" + dbColumnValue.length);
					System.out.println("dbColumnValue" + dbColumnValue[0]);
					for (int indexDbColumnValue = 0; indexDbColumnValue < dbColumnValue.length; indexDbColumnValue++) {
						Map columnAndType = getColumnNameAndType(dbColumnValue[indexDbColumnValue]);
						System.out.println("COLUMN  " + indexDbColumnValue + "   " + columnAndType.get("column"));
						System.out.println("TYPE  " + indexDbColumnValue + "   " + columnAndType.get("type"));
						System.out.println(
								"VALUE TO VERIFY  " + getColumnValueToverify(dbColumnValue[indexDbColumnValue]));
						String columnToVerify = getColumnValueToverify(dbColumnValue[indexDbColumnValue]);
						try {
							ResultSet rs = getResultSet(dBValidation[indexdBValidation].trim(), finalQuery.trim());
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

	public String finalQuery(Response response, String query, String parameters) {
		String parametersArray[] = parameters.split("PARAMETERSEP");
		for (int indexParametersArray = 0; indexParametersArray < parametersArray.length; indexParametersArray++) {
			String parameterAndValueArray[] = parametersArray[indexParametersArray].split("=");
			String parameter = parameterAndValueArray[0].trim();
			String value = XmlPath.from(response.asString()).get(parameterAndValueArray[1].trim());
			System.out.println("parameter+++++++++++----   " + parameter);
			System.out.println("value+++++++++++----   " + value);
			System.out.println("query+++++++++++----   " + query);
			query = query.replace(parameter, value);
		}
		return query;
	}

	public Map getXmlVerificationMap(String[] xmlVerification) {
		Map h = new HashMap();
		for (int index = 0; index < xmlVerification.length; index++) {
			String[] keyvalue = xmlVerification[index].split("=");
			h.put(keyvalue[0], keyvalue[1]);
		}
		return h;
	}

	public String processTemplate(Map templateData) throws IOException {
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

	public static ArrayList readCSVAndReturnMap() throws IOException {
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
		// System.out.println(list);
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
