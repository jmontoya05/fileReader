package com.reader.fileReader.service;

import com.reader.fileReader.feign.FileReaderFeignClient;
import com.reader.fileReader.model.File;
import com.reader.fileReader.model.SafetyIncident;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class XLSXReaderService {

    private final FileReaderFeignClient fileReaderFeignClient;

    @Autowired
    public XLSXReaderService(FileReaderFeignClient fileFeignClient) {
        this.fileReaderFeignClient = fileFeignClient;
    }

    public String uploadXLSXFile(File file) {
        try {
            String filePath = file.getFilePath();
            Workbook workbook = new XSSFWorkbook(filePath);
            Sheet sheet = workbook.getSheetAt(0);
            workbook.close();
            List<SafetyIncident> safetyIncidentList = readXLSX(sheet, workbook);

            return validateXLSX(safetyIncidentList);

        } catch (Exception e) {
            //Pendiente gestionar exepción
            e.printStackTrace();
            return "Se totió";
        }
    }

    private List<SafetyIncident> readXLSX(Sheet sheet, Workbook workbook) {
        List<SafetyIncident> safetyIncidentList = new ArrayList<>();

        for (Row row : sheet) {

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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                columnIndex++;
            }

            safetyIncidentList.add(safety);
        }
        return safetyIncidentList;
    }

    private String validateXLSX(List<SafetyIncident> safetyIncidents) {
        List<CompletableFuture<Boolean>> futures = safetyIncidents.stream()
                .map(safetyIncident -> CompletableFuture.supplyAsync(() -> fileReaderFeignClient.validateXLSX(safetyIncident)))
                .collect(Collectors.toList());

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        CompletableFuture<List<Boolean>> allResults = allOf.thenApply(v ->
                futures.stream()
                        .map(future -> future.join())
                        .collect(Collectors.toList())
        );

        long validCount = allResults.join().stream().filter(Boolean::booleanValue).count();
        long invalidCount = safetyIncidents.size() - validCount;

        return "Líneas validas: " + validCount +
                "\nLíneas invalidas: " + invalidCount;
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
