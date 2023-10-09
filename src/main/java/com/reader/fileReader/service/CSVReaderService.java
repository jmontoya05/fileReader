package com.reader.fileReader.service;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.reader.fileReader.feign.FileReaderFeignClient;
import com.reader.fileReader.model.File;
import com.reader.fileReader.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.util.List;

@Service
public class CSVReaderService {

    private final FileReaderFeignClient fileReaderFeignClient;

    @Autowired
    public CSVReaderService(FileReaderFeignClient fileFeignClient) {
        this.fileReaderFeignClient = fileFeignClient;
    }
    public String uploadCSVFile(File file) {
        try {
            String filePath = file.getFilePath();
            FileReader fileReader = new FileReader(filePath);
            CSVReader reader = new CSVReader(fileReader);

            CsvToBean<Person> csvToBean = new CsvToBeanBuilder<Person>(reader)
                    .withType(Person.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<Person> persons = csvToBean.parse();
            reader.close();
            fileReader.close();

            //Pendiente exepción
            return validateCSV(persons);

        } catch (Exception e) {
            return "Se totió";
        }
    }

    private String validateCSV(List<Person> persons){
        int valid = 0, invalid = 0;
        for (Person person : persons) {
            if (fileReaderFeignClient.validateCSV(person)) {
                valid++;
            } else {
                invalid++;
            }
        }
        return "Líneas validas: " + valid +
                "\nLíneas invalidas: " + invalid;
    }
}
