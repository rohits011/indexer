package com.smartcrew.rjh.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.CoreAdminParams.CoreAdminAction;
import org.apache.solr.common.util.ContentStreamBase;
import org.eclipse.jetty.client.HttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RJHService {
	
	private static WebClient client;
	String url="http://localhost:8983/solr/";
	String baseSolrUrl = "http://localhost:8983/solr/";
	
	public HashMap<String, Object> getHCPMetaData(String uri,String tenant,String hcp_system_name)
			throws IOException {

//		String uri="https://emlgbrab400.enron.hcp-demo.hcpdemo.com/rest/Test/";
//		String tenant="enron";
//		String hcp_system_name="hcp-demo.hcpdemo.com";
		
		HashMap<String, Object> result = new HashMap<>();

		String stringResponse = "No record Found";

		final Map<String, String> headers = new HashMap<String, String>();

		if (client == null) {
			client = new WebClient();
		}

		String domainAD = "audaxlabs.com";
		String userName = "bob.williams";
		String password = "Global@123";

		headers.put("Authorization", String.format("AD %s@%s:%s", userName, domainAD, password));
		headers.put("Accept", "application/json");

		final String[] queryString1 = uri.split("/rest/");
		String objectPath = "/" + queryString1[1];
		objectPath = objectPath.replace("%20", " ");
		objectPath = String.format("(objectPath:\\\"%s\\\") ", objectPath);

		String fullTenantName = String.format("%s.%s", tenant, hcp_system_name);
		final String hcpServerUri = String.format("https://%s/query", fullTenantName);
		final String hcpBody = String.format("{\"object\":{\"query\":\"%s\", \"verbose\":\"true\"}}", objectPath);

		Response response = client.postWithUri(hcpBody, hcpServerUri, headers, MediaType.APPLICATION_JSON_TYPE);
		if (response.getStatus() == 200) {
			stringResponse = response.readEntity(String.class);

			HashMap<String, ArrayList<Map<String, Object>>> resultmap = new HashMap<>();

			ObjectMapper mapper = new ObjectMapper();
			resultmap = mapper.readValue(stringResponse.toString(), HashMap.class);
			Map<String, Object> queryResult = (Map<String, Object>) resultmap.get("queryResult");
			ArrayList<Map<String, String>> resultSet = (ArrayList<Map<String, String>>) queryResult.get("resultSet");

			result.put("status", "success");
			result.put("statusCode", response.getStatus());
			result.put("data", resultSet);

		} else {
			System.out.println(String.format("ERROR: Error retrieving HCP retention value. Status code: %s, Tenant:%s",
					response.getStatus(), hcpServerUri));
			result.put("status", "failed");
			result.put("statusCode", response.getStatus());
			result.put("data", stringResponse);
		}

		return result;
	}

	
	public String deleteIndexdatabyQuery(String query,String index)
			throws IOException {
		String url1=url+index;
		String status="----Not Deleted----";
		
		try {
			SolrClient solr = new HttpSolrClient.Builder(url1).build();
	        solr.deleteByQuery(query);
	       // solr.deleteByQuery("*:*");
	        solr.commit();
	        System.out.println("Deleted");
	        status="----Deleted----";
	        
		}catch(Exception e) {
			
			 status="----Error:"+e+"----";
			 
		}
		
		return status;
	}
	
	public String deleteIndexdatabyId(String id,String index)
			throws IOException {
		String url1=url+index;
		String status="----Not Deleted----";
		
		try {
			SolrClient solr = new HttpSolrClient.Builder(url1).build();
	        solr.deleteById(id);
	        solr.commit();
	        status="----Deleted----";
	        
		}catch(Exception e) {
			
			 status="----Error:"+e+"----";
			 
		}
		
		return status;
	}

	
	public String indexdata(JSONArray doc,String index) throws SolrServerException, IOException {
		
		String status="----Data Not Indexed----";
//		List<String> data = new ArrayList<String>();
//		data.add(doc);
		String url1=url+index;
		
		try {
//		url = url+"/update?_=1688554035135&commitWithin=1000&overwrite=true&wt=json";
		SolrClient solr = new HttpSolrClient.Builder(url1).build();
    		CloseableHttpClient httpClient = HttpClients.custom().build();
    		
		//  DefaultHttpClient httpClient = new DefaultHttpClient();
          HttpPost post = new HttpPost(url1+"/update/json?wt=json&commit=true");
          StringEntity entity  = new StringEntity(doc.toString());
          entity.setContentType("application/json");
          

          post.setEntity(entity);                
          
//          post.setEntity();
          HttpResponse response = httpClient.execute(post);
          HttpEntity httpEntity = response.getEntity();
          InputStream in = httpEntity.getContent();

          String encoding = httpEntity.getContentEncoding() == null ? "UTF-8" : httpEntity.getContentEncoding().getName();
          encoding = encoding == null ? "UTF-8" : encoding;
          String responseText = IOUtils.toString(in, encoding);
          System.out.println("response Text is " + responseText);
          status="----Data Indexed----";
		
		}catch(Exception e) {
			
			 status="----Error:"+e+"----";
			 
		}
		
		return status;
	}
	
	public SolrDocumentList getindexdata(String query,String field,String index) throws SolrServerException, IOException {
		
	      //Preparing the Solr client    
		String url1=url+index;
	      SolrClient Solr = new HttpSolrClient.Builder(url1).build();    
	        
	      //Preparing the Solr query   
	      SolrQuery solrquery = new SolrQuery();    
	      solrquery.setQuery(query);    
//	      solrquery.setQuery("*:*"); 
	     
	      //Adding the field that has to be retrieved   
	      solrquery.addField(field);  
//	      solrquery.addField("*"); 
	     
	      //Executing query for data   
	      QueryResponse queryResponse = Solr.query(solrquery);    
	     
	      //Saving the results of the query   
	      SolrDocumentList docs = queryResponse.getResults();            
	           
	      //storing the operations   
	      Solr.commit();   
	      return docs;
	   }


	public void createIndex(String indexName) {
		    String coreName = indexName;
		
		    SolrClient client = new HttpSolrClient.Builder(baseSolrUrl).build();
		    CoreAdminRequest.Create createRequest = new CoreAdminRequest.Create();
		    createRequest.setCoreName(coreName);
		    createRequest.setInstanceDir("./" + coreName);
		    createRequest.setConfigSet("_default");
		    try {
				createRequest.process(client);
			} catch (SolrServerException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
	}   
	
    public List<String> getCores() {
        System.out.println("Building Solr server instance");
        HttpSolrClient solrClient=new HttpSolrClient.Builder(baseSolrUrl).build();    

        System.out.println("Requesting core list"); 
        CoreAdminRequest request = new CoreAdminRequest();
        request.setAction(CoreAdminAction.STATUS);
        CoreAdminResponse cores=null;

        try {
            cores = request.process(solrClient);
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(" Listing cores");
        List<String> coreList = new ArrayList<String>();
        for (int i = 0; i < cores.getCoreStatus().size(); i++) {
            coreList.add(cores.getCoreStatus().getName(i));
        }
        return coreList;

    }       
	
}
