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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
        int numThreads = 150;//número de hilos que se ejecutarán en paralelo
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<CompletableFuture<Boolean>> futures = persons.stream()
                .map(person -> CompletableFuture.supplyAsync(() -> fileReaderFeignClient.validateCSV(person), executorService))
                .collect(Collectors.toList());
        //Crea un CompleteFuture que se completa cuando los individuales se completen
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allOf.get();//Espera que todas las tareas se completen
        } catch (Exception e) { //pendiente manejar excepción
            System.out.println("Error en las tareas");
        }
        long validCount = futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        return false;
                    }
                })
                .filter(Boolean::booleanValue)//Filtra solo por true
                .count();
        long invalidCount = persons.size() - validCount;
        executorService.shutdown();
        return "Líneas válidas: " + validCount + "\nLíneas inválidas: " + invalidCount;
    }
}
