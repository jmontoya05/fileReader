package com.reader.fileReader.service;

import com.reader.fileReader.exception.BadRequestsException;
import com.reader.fileReader.exception.InternalServerErrorException;
import com.reader.fileReader.feign.FileReaderFeignClient;
import com.reader.fileReader.model.File;
import com.reader.fileReader.model.SafetyIncident;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class XLSXReaderService {

    private final FileReaderFeignClient fileReaderFeignClient;

    @Autowired
    public XLSXReaderService(FileReaderFeignClient fileFeignClient) {
        this.fileReaderFeignClient = fileFeignClient;
    }

    public String uploadXLSXFile(File file) {
        String filePath = file.getFilePath();
        try (Workbook workbook = new XSSFWorkbook(filePath)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<SafetyIncident> safetyIncidentList = readXLSX(sheet, workbook);

            return validateXLSX(safetyIncidentList);

        } catch (NullPointerException | IllegalArgumentException e) {
            throw new BadRequestsException("El atributo filePath es requerido: " + e.getMessage());
        } catch (IOException | InvalidOperationException e) {
            throw new BadRequestsException("No se encontró el archivo en la ruta especificada: " + e.getMessage());
        }
    }

    private List<SafetyIncident> readXLSX(Sheet sheet, Workbook workbook) {
        List<SafetyIncident> safetyIncidentList = new ArrayList<>();
        boolean firstFile = true;
        for (Row row : sheet) {
            if (firstFile) {
                firstFile = false;
            } else {

                SafetyIncident safety = new SafetyIncident();
                int columnIndex = 0;

                for (Cell cell : row) {
                    String columnName = sheet.getRow(0).getCell(columnIndex).getStringCellValue();
                    String propertyName = mapProperties().get(columnName);

                    if (propertyName != null) {
                        String cellValue;
                        switch (cell.getCellType()) {
                            case STRING:
                                cellValue = cell.getStringCellValue();
                                break;
                            case NUMERIC:
                                double numericValue = cell.getNumericCellValue();
                                cellValue = String.valueOf(numericValue);
                                break;
                            case FORMULA:
                                FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                                CellValue cellVal = evaluator.evaluate(cell);
                                cellValue = cellVal.formatAsString();
                                break;
                            default:
                                cellValue = null;
                                break;
                        }
                        try {
                            BeanUtils.setProperty(safety, propertyName, cellValue);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new InternalServerErrorException("Hubo un problema con el formateo del archivo: " + e.getMessage());
                        }
                    }
                    columnIndex++;
                }
                safetyIncidentList.add(safety);
            }
        }
        return safetyIncidentList;
    }

    private String validateXLSX(List<SafetyIncident> safetyIncidents) {
        int numThreads = 150;
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<CompletableFuture<Boolean>> futures = safetyIncidents.stream()
                .map(safetyIncident -> CompletableFuture.supplyAsync(() -> fileReaderFeignClient.validateXLSX(safetyIncident), executorService))
                .collect(Collectors.toList());
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allOf.get();
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
                .filter(Boolean::booleanValue)
                .count();
        long invalidCount = safetyIncidents.size() - validCount;
        executorService.shutdown();
        return "Líneas válidas: " + validCount + "\nLíneas inválidas: " + invalidCount;
    }

    private Map<String, String> mapProperties() {
        Map<String, String> columnToPropertyMap = new HashMap<>();
        columnToPropertyMap.put("Date", "date");
        columnToPropertyMap.put("Injury Location", "injuryLocation");
        columnToPropertyMap.put("Gender", "gender");
        columnToPropertyMap.put("Age Group", "ageGroup");
        columnToPropertyMap.put("Incident Type", "incidentType");
        columnToPropertyMap.put("Days Lost", "daysLost");
        columnToPropertyMap.put("Plant", "plant");
        columnToPropertyMap.put("Report Type", "reportType");
        columnToPropertyMap.put("Shift", "shift");
        columnToPropertyMap.put("Department", "department");
        columnToPropertyMap.put("Incident Cost", "incidentCost");
        columnToPropertyMap.put("WkDay", "wkDay");
        columnToPropertyMap.put("Month", "month");
        columnToPropertyMap.put("Year", "year");

        return columnToPropertyMap;
    }

}
