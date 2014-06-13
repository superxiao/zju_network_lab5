package org.zju.lab5_server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

public class HttpProtocol {
	
	public String processInput(String input) {
		// For http protocol details see the http wiki page.
		Scanner scanner = new Scanner(input);
		String line = scanner.nextLine();
		String[] tokens = line.split(" ");
		if (tokens[0].compareTo("GET") == 0
				&& tokens[2].compareTo("HTTP/1.1") == 0) {
			// Example: GET /index.html HTTP/1.1
			// Should return page tokens[1]. Here just return a string
			String content;
			try {
				String url = tokens[1];
				if(url.compareTo("/") == 0)
					url = "index.html";
				content = new String(Files.readAllBytes(Paths.get(url)));
				return "HTTP/1.1 200 OK\r\n\r\n" + content;
			} catch (IOException e) {
				e.printStackTrace();
				return "HTTP/1.1 404 Not Found\r\n\r\n<html><body>404 Not Found</body></html>";
			}
		}
		else if(tokens[0].compareTo("POST") == 0
				&& tokens[2].compareTo("HTTP/1.1") == 0)
		{
			return "HTTP/1.1 200 OK\r\n\r\nShould return page from another server!";
		}
		return null;
	}
}
