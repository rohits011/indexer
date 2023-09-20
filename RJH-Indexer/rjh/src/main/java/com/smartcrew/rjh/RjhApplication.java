package com.smartcrew.rjh;

import java.io.IOException;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RjhApplication {

	public static void main(String[] args) throws SolrServerException, IOException {
		SpringApplication.run(RjhApplication.class, args);
	}

}
