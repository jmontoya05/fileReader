package com.reader.fileReader.controller;

import com.reader.fileReader.exception.BadRequestsException;
import com.reader.fileReader.model.File;
import com.reader.fileReader.service.CSVReaderService;
import com.reader.fileReader.service.XLSXReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/validate")
public class FileReaderController {
    private final CSVReaderService csvReaderService;
    private final XLSXReaderService xlsxReaderService;
    @Autowired
    public FileReaderController(CSVReaderService csvReaderService, XLSXReaderService xlsxReaderService) {
        this.csvReaderService = csvReaderService;
        this.xlsxReaderService = xlsxReaderService;
    }

    @PostMapping
    public ResponseEntity<String> uploadCSVFile(@RequestBody File file) {
        String typeFile = file.getType();
        try {
            switch (typeFile.toLowerCase()){
                case "csv":
                    return new ResponseEntity<>(csvReaderService.uploadCSVFile(file), HttpStatus.OK);
                case "xlsx":
                    return new ResponseEntity<>(xlsxReaderService.uploadXLSXFile(file), HttpStatus.OK);
                default:
                    throw new BadRequestsException("Tipo de archivo no valido");
            }
        } catch (NullPointerException e){
            throw new BadRequestsException("El atributo type es requerido: " + e.getMessage());
        }
    }
}
