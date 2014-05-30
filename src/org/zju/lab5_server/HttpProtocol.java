package org.zju.lab5_server;

public class HttpProtocol {
	public String processInput(String input) {
		// For http protocol details see the http wiki page.
		String[] tokens = input.split(" ");
		if (tokens[0].compareTo("GET") == 0
				&& tokens[2].compareTo("HTTP/1.1") == 0) {
			// Example: GET /index.html HTTP/1.1
			// Should return page tokens[1]. Here just return a string
			return "HTTP/1.1 200 OK\r\n\r\n<html><body>"
					+ "This is an empty page (except for a button)!<br>"
					+ "<form name=\"input\" method=\"post\">"
					+ "<input type=\"submit\" value=\"A cute button\"></form> "
					+ "</body></html>";
		}
		else if(tokens[0].compareTo("POST") == 0
				&& tokens[2].compareTo("HTTP/1.1") == 0)
		{
			return "Should return page from another server!";
		}
		return null;
	}
}
