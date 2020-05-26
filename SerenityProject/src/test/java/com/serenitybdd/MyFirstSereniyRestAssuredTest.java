package com.serenitybdd;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.serenitybdd.cucumber.steps.serenity.AddPracticeSteps;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;

import net.thucydides.core.annotations.Steps;
import net.thucydides.junit.annotations.TestData;



@RunWith(SerenityParameterizedRunner.class)
public class MyFirstSereniyRestAssuredTest {
		
	@Steps
    AddPracticeSteps addPracticeSteps ;
	
	@TestData
	public static Collection<Object[]> testData() throws IOException{
		List<Object[]> results = new ArrayList<Object[]>();
		ArrayList<Map<String, String>> testDataMap = readCSVAndReturnMap();
		for (int i = 0; i < testDataMap.size(); i++) {
			Map<String, String> firstParameter = testDataMap.get(i);
			results.add(new Object[] { firstParameter, firstParameter.get("Test Case ID") });
		}
		return results;
    }
	
	Map templateData;
	String testCaseName;
	
	public MyFirstSereniyRestAssuredTest(Map<String, String> keyVals, String testcaseName) {
		this.templateData = keyVals;
		this.testCaseName = testcaseName;
	}
	
    @Test
	public void addPracticeTest() throws IOException {
		String request = null;
		request = processTemplate(templateData);
		System.out.println(request);
		addPracticeSteps.submitRequest(request, testCaseName);

		/////// Xpath Verification in Response:
		Map xmlVerificationMap = getXmlVerificationMap(templateData.get("Status").toString().split(":"));
		Iterator xmlVerificationEntries = xmlVerificationMap.entrySet().iterator();

		while (xmlVerificationEntries.hasNext()) {
			Map.Entry thisEntry = (Map.Entry) xmlVerificationEntries.next();
			String key = thisEntry.getKey().toString();
			String value = thisEntry.getValue().toString();
			addPracticeSteps.checkResponse(value, key);
		}

		////////////////////////////////////////////////////////////////////////////////////

	}
    
    public Map getXmlVerificationMap(String[] xmlVerification) {
		Map h = new HashMap();
		for(int index =0;index < xmlVerification.length;index++) {
			String [] keyvalue = xmlVerification[index].split("=");
			h.put(keyvalue[0], keyvalue[1]);
		}
		return h;
	}
    
    public String processTemplate(Map templateData) throws IOException {
    	 Configuration cfg = new Configuration(new Version("2.3.23"));
         cfg.setDefaultEncoding("UTF-8");
         cfg.setDirectoryForTemplateLoading(new File("src\\test\\resources\\restassuredrequests"));
         Template t = cfg.getTemplate("addPractice.xml");
         StringWriter out  = new StringWriter();
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
        File file = new File("src\\test\\resources\\csv\\add_practice_xls_name_csv.csv");
        Reader reader = new FileReader(file);
        Iterator<Map<String, String>> iterator = new CsvMapper()
                .readerFor(Map.class)
                .with(CsvSchema.emptySchema().withHeader())
                .readValues(reader);
        while (iterator.hasNext()) {
         //   keyVals = iterator.next();
            list.add(iterator.next());

        }
      //  System.out.println(list);
        return list;
    }


}