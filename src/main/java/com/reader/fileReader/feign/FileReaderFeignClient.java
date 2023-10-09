package com.reader.fileReader.feign;

import com.reader.fileReader.model.Person;
import com.reader.fileReader.model.SafetyIncident;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "fileValidator", url = "${file-validator.url}")
public interface FileReaderFeignClient {
    @PostMapping("/csv")
    boolean validateCSV(@RequestBody Person person);

    @PostMapping("/xlsx")
    boolean validateXLSX(@RequestBody SafetyIncident safetyIncident);
}
