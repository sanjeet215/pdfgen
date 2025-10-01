package com.doc.pdfgen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.doc.baseservice", "com.doc.pdfgen"})
@EntityScan("com.doc.baseservice")
@EnableJpaRepositories("com.doc.baseservice.repository")
public class PdfgenApplication {

	public static void main(String[] args) {
		SpringApplication.run(PdfgenApplication.class, args);
	}

}
