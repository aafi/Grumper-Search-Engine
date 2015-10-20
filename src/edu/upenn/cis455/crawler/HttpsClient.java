package edu.upenn.cis455.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Date;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

import edu.upenn.cis455.bean.DocumentRecord;

/**
 * This class creates an HttpsClient that can fetch documents from a given https
 * url
 * 
 * @author cis455
 *
 */
public class HttpsClient {

	private URL sourceUrl;
	private static final String CONTENT_TYPE_HEADER = "Content-Type";
	private static final String CONTENT_LENGTH_HEADER = "Content-Length";
	private static final String XML = "xml";
	private static final String HTML = "html";

	public HttpsClient(URL url) throws UnknownHostException, IOException {
		sourceUrl = url;
	}

	/**
	 * fetches the document from the given url
	 * 
	 * @return
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public DocumentRecord getDocument() throws UnknownHostException,
			IOException, SAXException, ParserConfigurationException,
			NumberFormatException {
		boolean isHtml = false;
		boolean isXml = false;
		DocumentRecord documentRecord = null;
		// first send head
		HttpResponse response = sendHead();
		//System.out.println(sourceUrl + " - " + response);
		if (response != null && response.getHeaders() != null
				&& response.getHeaders().containsKey(CONTENT_LENGTH_HEADER)
				&& response.getHeaders().containsKey(CONTENT_TYPE_HEADER)) {
			String contentType = response.getHeaders().get(CONTENT_TYPE_HEADER)
					.get(0);
			int contentLength = Integer.valueOf(response.getHeaders()
					.get(CONTENT_LENGTH_HEADER).get(0));
			if (!(contentType.contains(HTML) || contentType.contains(XML))
					|| contentLength > XPathCrawler.getMaxSize()) {
				// return if document is not xml || html or if length is larger
				// than max size
				System.out
						.println(sourceUrl
								+ " : Not fetching file due to type mis match or larger size");
				System.out.println("contentType - " + contentType);
				System.out.println("contentLength - " + contentLength);
				System.out.println("max size - " + XPathCrawler.getMaxSize());
				return documentRecord;
			}
		}
		// then send get
		response = sendRequest();
		// System.out.println(response);

		if (response != null && response.getResponseCode().equals("200")
				&& response.getHeaders().containsKey(CONTENT_TYPE_HEADER)) {
			if (response.getHeaders().get(CONTENT_TYPE_HEADER).get(0)
					.contains(HTML)) {
				isHtml = true;
			} else if (response.getHeaders().get(CONTENT_TYPE_HEADER).get(0)
					.contains(XML)) {
				isXml = true;
			}
			long lastCrawled = (new Date()).getTime();
			if (response.getHeaders().containsKey("Date")) {
				String dateString = response.getHeaders().get("Date").get(0);
				Date date = DocumentRecord.getDate(dateString);
				if (date != null) {
					lastCrawled = date.getTime();
				}
			}
			documentRecord = new DocumentRecord(sourceUrl.toString(),
					response.getData(), isHtml, isXml, lastCrawled);
		}

		return documentRecord;
	}

	public HttpResponse sendHead() throws IOException {

		HttpsURLConnection connection = (HttpsURLConnection) sourceUrl
				.openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("HEAD");
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
				connection.getOutputStream());
		// outputStreamWriter.write("HEAD " + sourceUrl + " HTTPS/1.0\r\n");
		outputStreamWriter.write("User-Agent: cis455crawler\r\n");
		outputStreamWriter.write("\r\n");
		outputStreamWriter.flush();
		outputStreamWriter.close();
		HttpResponse httpResponse = parseResponse(connection);
		connection.disconnect();
		return httpResponse;
	}

	public HttpResponse sendRequest() throws IOException {
		HttpsURLConnection connection = (HttpsURLConnection) sourceUrl
				.openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("GET");
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
				connection.getOutputStream());
		// outputStreamWriter.write("GET " + sourceUrl + " HTTPS/1.0\r\n");
		outputStreamWriter.write("User-Agent: cis455crawler\r\n");
		outputStreamWriter.write("Accept: text/html,application/xml\r\n");
		outputStreamWriter.write("\r\n");
		outputStreamWriter.flush();
		outputStreamWriter.close();
		HttpResponse httpResponse = parseResponse(connection);
		connection.disconnect();
		return httpResponse;
	}

	public HttpResponse parseResponse(HttpsURLConnection connection)
			throws IOException {
		// connection.setDoInput(true);
		if (connection.getResponseCode() != 200) {
			return null;
		}
		InputStream connectionInputStream = connection.getInputStream();
		InputStreamReader connectionInputStreamReader = new InputStreamReader(
				connectionInputStream);
		BufferedReader connectionBufferedReader = new BufferedReader(
				connectionInputStreamReader);
		HttpResponse response = parseResponse(connectionBufferedReader);
		response.setResponseCode("" + connection.getResponseCode());
		// get the first line -
		if (connection.getHeaderFields() != null
				&& connection.getHeaderFields().get(null) != null
				&& connection.getHeaderFields().get(null).get(0) != null) {
			String[] firstLineSplit = connection.getHeaderFields().get(null)
					.get(0).split(" ");
			if (firstLineSplit.length < 3) {
				return null;
			}
			if (firstLineSplit[0].trim().split("/").length < 2) {
				return null;
			}
			response.setProtocol((firstLineSplit[0].trim().split("/")[0]));
			response.setVersion((firstLineSplit[0].trim().split("/")[1]));
			response.setResponseCode(firstLineSplit[1].trim());
			response.setResponseCodeString(firstLineSplit[2].trim());
		}
		response.setHeaders(connection.getHeaderFields());
		connectionBufferedReader.close();
		connectionInputStreamReader.close();
		connectionInputStream.close();
		return response;
	}

	/**
	 * parses the http response from the server into an HttpResponse object
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public HttpResponse parseResponse(BufferedReader in) throws IOException {
		HttpResponse response = new HttpResponse();
		String line;
		StringBuilder responseBody = new StringBuilder();
		while ((line = in.readLine()) != null) {
			responseBody.append(line + "\r\n");
		}
		response.setData(responseBody.toString());
		return response;
	}

}
