package com.reader.fileReader.service;

import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.reader.fileReader.exception.BadRequestsException;
import com.reader.fileReader.exception.InternalServerErrorException;
import com.reader.fileReader.feign.FileReaderFeignClient;
import com.reader.fileReader.model.File;
import com.reader.fileReader.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class CSVReaderService {

    private final FileReaderFeignClient fileReaderFeignClient;

    @Autowired
    public CSVReaderService(FileReaderFeignClient fileFeignClient) {
        this.fileReaderFeignClient = fileFeignClient;
    }

    public String uploadCSVFile(File file) {

        String filePath = file.getFilePath();
        try (
                FileReader fileReader = new FileReader(filePath);
                CSVReader reader = new CSVReader(fileReader)
        ) {
            CsvToBean<Person> csvToBean = new CsvToBeanBuilder<Person>(reader)
                    .withType(Person.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            List<Person> persons = csvToBean.parse();

            return validateCSV(persons);

        } catch (NullPointerException | IllegalArgumentException e) {
            throw new BadRequestsException("El atributo filePath es requerido: " + e.getMessage());
        } catch (IOException e) {
            throw new BadRequestsException("No se encontró el archivo en la ruta especificada: " + e.getMessage());
        }
    }

    private String validateCSV(List<Person> persons) {
        int numThreads = 150;//número de hilos que se ejecutarán en paralelo
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<CompletableFuture<Boolean>> futures = persons.stream()
                .map(person -> CompletableFuture.supplyAsync(() -> fileReaderFeignClient.validateCSV(person), executorService))
                .collect(Collectors.toList());
        //Crea un CompleteFuture que se completa cuando los individuales se completen
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allOf.get();//Espera que todas las tareas se completen
        } catch (InterruptedException | ExecutionException e) {
            throw new InternalServerErrorException("Hubo un problema con la conexión al servicio validador: " + e.getMessage());
        }
        long validCount = futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException | ExecutionException e) {
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
