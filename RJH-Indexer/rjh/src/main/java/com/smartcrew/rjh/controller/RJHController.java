package com.smartcrew.rjh.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocumentList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.smartcrew.rjh.Service.RJHService;



@RestController
@RequestMapping("RJH")
@CrossOrigin(origins = "*")
public class RJHController {

	@Autowired 
	RJHService rjhService;
	
	
	@PostMapping("IndexAndUpdatedata")
	public ResponseEntity<String> indexMetadata(
			@RequestParam("loc") String loc,
			@RequestParam("tenant") String tenant,
			@RequestParam("hcp_system_name") String hcp_system_name,@RequestParam("index") String index) throws IOException, SolrServerException  {

		HashMap<String, Object> result;
		String response="----Unkown Error----";
		result = rjhService.getHCPMetaData(loc, tenant, hcp_system_name);
		
			if(result.get("status")=="success") {
				ArrayList<Map<String, String>> metadata=(ArrayList<Map<String, String>>)result.get("data");
				JSONArray array = new JSONArray(metadata);
					response=rjhService.indexdata(array,index);
			}

		return ResponseEntity.ok(response);
	}
	
	@DeleteMapping("DeleteIndexdatabyQuery")
	public ResponseEntity<String> deleteIndexdatabyQuery(@RequestParam("query") String query,String index) throws SolrServerException, IOException  {

		String result = rjhService.deleteIndexdatabyQuery(query,index);
		
		return ResponseEntity.ok(result);
	}
	
	@DeleteMapping("DeleteIndexdatabyId")
	public ResponseEntity<String> deleteIndexdatabyId(@RequestParam("id") String id,String index) throws SolrServerException, IOException  {

		String result = rjhService.deleteIndexdatabyId(id,index);
		
		return ResponseEntity.ok(result);
	}
	
	@GetMapping("GetIndexData")
	public ResponseEntity<SolrDocumentList> GetIndexData(String query,String field ,String index) throws IOException, SolrServerException  {

		SolrDocumentList response = rjhService.getindexdata(query, field,index);

		return ResponseEntity.ok(response);
	}
	
	@PostMapping("createIndex")
	public ResponseEntity<Map<String,String>> createIndex(@RequestParam("indexName") String indexName) throws IOException, SolrServerException  {
        
		Map<String,String> result = new HashMap<String, String>();
		
		 rjhService.createIndex(indexName);
		 result.put("Message", " Index Successfully Created");

		return ResponseEntity.ok(result);
	}
	
	@GetMapping("GetCores")
	public ResponseEntity<List<String>> GetCores(String query,String field) throws IOException, SolrServerException  {

		List<String> result = new ArrayList<String>();
	    result =  rjhService.getCores();

		return ResponseEntity.ok(result);
	}

	
}
