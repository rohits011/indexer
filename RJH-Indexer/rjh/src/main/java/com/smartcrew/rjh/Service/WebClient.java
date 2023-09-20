package com.smartcrew.rjh.Service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.springframework.beans.factory.annotation.Value;

public class WebClient {
	
	
	@Value("${Debug}")
	private boolean debug;
	
	private Client client;
	private String serverUri;
	
	@Value("${adminServerUri}")
	private String adminServer; 
	
	@Value("${searchServerUri}")
	private String searchServer;
	
	@Value("${connectTimeout}")
	private int connectTimeout;
	
	
	// Instrumentation method to remove password from body.
	private static String removeDetails(String body) {
		body = body.replaceAll("\\\\:", ":");
		body = body.replaceAll("&-p=[A-Za-z0-9!@$%# ]+", "").replaceAll("&password=[A-Za-z0-9!@$%# ]+", "");
		return body;
	}
	
	

	//private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

	/**
	 * Default constructor, created for backwards compatibility. By default, the
	 * Web client will be of the type Search
	 *
	 * @throws IOException
	 *             if no configuration file is loaded with the application.
	 */
	public WebClient() throws IOException {
		this(false);
	}

	/**
	 * Constructor for a new Web Client. This constructor can be of two ways:
	 * <ul>
	 * <li>An Admin Web Client, which will send requests to the URL on the
	 * property <code>adminServerUri</code> property</li>
	 * <li>a Search Web Client, which will send request to the URL specified in
	 * the <code>searchServerUri</code> property</li>
	 * </ul>
	 *
	 * @param forAdminRequests
	 *            True if this Web Client must be of the Admin type, false if it
	 *            must be of the Search Type.
	 * @throws IOException
	 *             if the configuration can not be found
	 */
	public WebClient(boolean forAdminRequests) throws IOException {

		ClientConfig configuration = new ClientConfig();
		int timeOut = connectTimeout;
		configuration = configuration.property(ClientProperties.CONNECT_TIMEOUT, timeOut);
		// Edit by AJAYTYAGI

		try {
			SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(new TrustSelfSignedStrategy()).build();
			HostnameVerifier allowAllHosts = new NoopHostnameVerifier();

			client = ClientBuilder.newBuilder().sslContext(sslContext).hostnameVerifier(allowAllHosts).build();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		// Edit end

		String adminServerUri = adminServer;
		String searchServerUri = searchServer;
		// based on the specified flag, we create the correct client.
		if (forAdminRequests) {
			this.serverUri = adminServerUri;
		} else {
			this.serverUri = searchServerUri;
		}
//		this.debug = debug;
	}

	public WebClient(String serverUri) {
		client = ClientBuilder.newClient();
		this.serverUri = serverUri;
		//this.debug = debug;
//		if (debug) {
//			client.register(new LoggingFilter(Logger.getLogger(this.getClass().getName()), true));
//		}
	}

	/**
	 * @param response
	 *            - api response object
	 */
	private String getErrorMessage(Response response, String target) {
		String message = "";
		if (response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
			message = String.format(
					"API request %s returned Unauthorized error message.  Please update or check credentials. Error code: %d. Response body: %s",
					target, response.getStatus(), response.readEntity(String.class));
		} else if (response.getStatus() == Response.Status.FORBIDDEN.getStatusCode()) {
			message = String.format(
					"API request %s returned Forbidden error message. Check your account has permission and is not locked.  Error code: %d. Response body: %s",
					target, response.getStatus(), response.readEntity(String.class));
		} else if (response.getStatus() == Response.Status.REQUEST_TIMEOUT.getStatusCode()) {
			message = String.format("API request %s timed out. Error code %d. Response body: %s", target,
					response.getStatus(), response.readEntity(String.class));
		} else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
			message = String.format(
					"API endpoint or requested document %s not found. Please check network connectivity.  Error code %d. Response body: %s",
					target, response.getStatus(), response.readEntity(String.class));
		} else {
			message = String.format("API request %s returned Error code %d. Response body: %s", target,
					response.getStatus(), response.readEntity(String.class));
		}
		return message;
	}

	// *** Request methods section ***
	private Response webTargetGet(WebTarget target, String path) {
		Response response;
		long startTime, endTime;
		startTime = System.currentTimeMillis();
		response = target.path(path).request().get(Response.class);
		if (debug) {
			endTime = System.currentTimeMillis();
			logRequestInformation(path, response, startTime, endTime);
		}
		if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
			String message = getErrorMessage(response, path);
			//logger.error(message);
//			throw new WebClientException(message);
		}
		return response;
	}

	private void logRequestInformation(String path, Response response, long startTime, long endTime) {
		if (Response.Status.Family.familyOf(response.getStatus()) != Response.Status.Family.SUCCESSFUL) {
			// as the request was not successful, we need to print the status
			// for debugging
			//logger.warn(String.format("0000 GET URL: %s/%s\tDuration: %d ms", serverUri, path, endTime - startTime));
		}
	}

	private Response webTargetPost(WebTarget target, String path, Object body, MediaType mediaType) {
		Response response;
		long startTime, endTime;
		startTime = System.currentTimeMillis();
		response = target.path(path).request().post(Entity.entity(body, mediaType));
		if (debug) {
			endTime = System.currentTimeMillis();

			//logger.info(String.format("0000 POST URL: %s/%s\tDuration: %d ms\tBody: %s\tStatus:%d", serverUri, path,
					//endTime - startTime, removeDetails(body.toString()), response.getStatus()));
		}
		if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
			String message = getErrorMessage(response, path);
			//logger.error(message);
//			throw new WebClientException(message, response.getStatusInfo());
		}
		return response;
	}

	private Response builderGet(Invocation.Builder requestBuilder, String path) {
		Response response = null;
		int retryCount = 0;
		while (true) {
			try {
				long startTime, endTime;
				startTime = System.currentTimeMillis();
				response = requestBuilder.get(Response.class);

				if (debug) {
					endTime = System.currentTimeMillis();
					logRequestInformation(path, response, startTime, endTime);
				}
				if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
					String message = getErrorMessage(response, path);
					//logger.error(message);
//					throw new WebClientException(message, response.getStatusInfo());
				} else {
					break;
				}
			} catch (ProcessingException pe) {
				if (retryCount++ > 3) {
					throw new ProcessingException(pe.getMessage(), new Throwable("Number of retries (4) exceeded"));
				}
			//	logger.error(String.format("Error accessing API. Retry in %d ms", 200 * retryCount));
				try {
					Thread.sleep(200 * retryCount);
				} catch (InterruptedException ie) {
					throw pe;
				}
				//logger.error(pe.getMessage());
			}
		}
		return response;
	}

	private Response builderPost(Invocation.Builder requestBuilder, String url, String path, Object body,
			MediaType mediaType) {

		Response response = null;
		int retryCount = 0;
		while (true) {
			try {
				long startTime, endTime;
				startTime = System.currentTimeMillis();
				body = removeDetails(body.toString());
				body = removeDetails(body.toString());
				response = requestBuilder.post(Entity.entity(body, mediaType));
				if (debug) {
					endTime = System.currentTimeMillis();
					//logger.info(String.format("0000 POST URL: %s/%s\tDuration: %d ms\tBody: %s\tStatus: %d", url, path,
							//endTime - startTime, removeDetails(body.toString()), response.getStatus()));
				}

				// TODO: check if this is used
				if (!response.getStatusInfo().getFamily().equals(Response.Status.Family.SUCCESSFUL)) {
					String message = getErrorMessage(response, path);
					//logger.error(message);
//					throw new WebClientException(message);
				} else {
					break;
				}
			} catch (ProcessingException pe) {
				if (retryCount++ > 3) {
					throw new ProcessingException(pe.getMessage(), new Throwable("Number of retries (4) exceeded"));
				}
			//	logger.error(String.format("Error accessing API. Retry in %d ms", 200 * retryCount));
				try {
					Thread.sleep(200 * retryCount);
				} catch (InterruptedException ie) {
					throw pe;
				}
				//logger.error(pe.getMessage());
			}
		}
		return response;
	}
	// *** End of request methods section ***

	public Response get(String id, String relativePath) {
		return webTargetGet(client.target(serverUri), relativePath + id);
	}

	public Response getWithUri(String uri, Map<String, String> headers) {
		WebTarget target = client.target(uri);
		Invocation.Builder requestBuilder = target.request();
		for (String headerKey : headers.keySet()) {
			requestBuilder = requestBuilder.header(headerKey, headers.get(headerKey));
		}

		return builderGet(requestBuilder, uri);
	}

	public Response get(String relativePath, Map<String, String> headers) {
		WebTarget target = client.target(serverUri + relativePath);
		Invocation.Builder requestBuilder = target.request();
		for (String headerKey : headers.keySet()) {
			requestBuilder = requestBuilder.header(headerKey, headers.get(headerKey));
		}

		return builderGet(requestBuilder, target.getUri().toASCIIString());

	}

	public Response post(Object body, String relativePath) {
		return this.post(body, relativePath, MediaType.APPLICATION_JSON_TYPE);

	}

	public Response post(Object body, String relativePath, MediaType mediaType) {
		WebTarget target = client.target(serverUri);
		return webTargetPost(target, relativePath, body, mediaType);
	}

	public Response post(Object body, String relativePath, Map<String, String> headers, MediaType mediaType) {
		WebTarget target = client.target(serverUri);
		Invocation.Builder requestBuilder = target.path(relativePath).request();

		for (String headerKey : headers.keySet()) {
			requestBuilder = requestBuilder.header(headerKey, headers.get(headerKey));
		}

		return builderPost(requestBuilder, serverUri, relativePath, body, mediaType);
	}

	public Response postWithUri(Object body, String uri, Map<String, String> headers, MediaType mediaType) {
		WebTarget target = client.target(uri);
		Invocation.Builder requestBuilder = target.request();

		for (String headerKey : headers.keySet()) {
			requestBuilder = requestBuilder.header(headerKey, headers.get(headerKey));
		}

		return builderPost(requestBuilder, uri, "", body, mediaType);
	}
	
	
}
