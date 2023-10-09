package com.reader.fileReader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FileReaderApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileReaderApplication.class, args);
	}

}
