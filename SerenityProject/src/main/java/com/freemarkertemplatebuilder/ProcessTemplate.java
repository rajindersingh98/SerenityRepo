package com.freemarkertemplatebuilder;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ProcessTemplate {
    public static void main(String args[]) throws IOException {
        Configuration cfg = new Configuration(new Version("2.3.23"));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setDirectoryForTemplateLoading(new File("src\\test\\resources\\restassuredrequests"));
        Template t = cfg.getTemplate("addPractice.xml");

        ArrayList<Map<String, String>> csvDataList=  readCSVAndReturnMap();

        for(int index =0 ;index < csvDataList.size() ;index ++) {
            Map templateData = csvDataList.get(index);

            try (
                    StringWriter out = new StringWriter()) {

                t.process(templateData, out);
                System.out.println("AFTER REPLACYING" +index +index + out.getBuffer().toString());

                out.flush();
            } catch (TemplateException e) {
                e.printStackTrace();
            }
        }


    }

    public static ArrayList readCSVAndReturnMap() throws IOException {
        Map<String, String> keyVals = null;
        ArrayList<Map> list = new ArrayList<>();
        File file = new File("src\\test\\resources\\csv\\addPractice.csv");
        Reader reader = new FileReader(file);
        Iterator<Map<String, String>> iterator = new CsvMapper()
                .readerFor(Map.class)
                .with(CsvSchema.emptySchema().withHeader())
                .readValues(reader);
        while (iterator.hasNext()) {
         //   keyVals = iterator.next();
            list.add(iterator.next());

        }
        System.out.println(list);
        return list;
    }

}
