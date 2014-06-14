package org.zju.lab5_server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HttpProtocol {

	public String processInput(String input) {
		// For http protocol details see the http wiki page.
		String[] lines = input.split("[\r\n]+");
		String[] tokens = lines[0].split(" ");
		if (tokens[0].compareTo("GET") == 0
				&& tokens[2].startsWith("HTTP/1.")) {
			// Example: GET /index.html HTTP/1.1
			// Should return page tokens[1]. Here just return a string
			String content;
			try {
				String url = tokens[1];
				if (url.compareTo("/") == 0)
					url = "index.html";
				content = new String(Files.readAllBytes(Paths.get(url)));
				return "HTTP/1.1 200 OK\r\n\r\n" + content + "\r\n";
			} catch (IOException e) {
				e.printStackTrace();
				return "HTTP/1.1 404 Not Found\r\n\r\n<html><body>404 Not Found</body></html>\r\n";
			}
		} else if (tokens[0].compareTo("POST") == 0
				&& tokens[2].startsWith("HTTP/1.")) {

			try {
				String formLine = lines[lines.length - 1];
				String[] inputs = formLine.split("&");
				String ip = "";
				if (inputs[0].startsWith("ip=")) {
					ip = inputs[0].substring(3);
					int port = 0;
					if(inputs[1].startsWith("port="))
						port = Integer.parseInt(inputs[1].substring(5));
					return GetSecondPageFromServer(ip, port);
				} else if (inputs[0].startsWith("source=server")) {
					// 返回页面2的信息
					String url = "second_page.html";
					String response;
					response = "HTTP/1.1 200 OK\r\n\r\n" + new String(Files.readAllBytes(Paths.get(url)));
					DesEncryption encryption = new DesEncryption();
					byte[] bytes = encryption.DES_Encrypt(encryption.ToBytes(response), "1234567890");
					String responseEn = encryption.BytesToString(bytes);
					bytes = encryption.ToBytes(responseEn);
					System.out.println("encoded bytes are " + bytes.toString() + "\n");
					bytes = encryption.DES_Decrypt(encryption.ToBytes(responseEn), "1234567890");
					System.out.println("Encoded is:\n" + responseEn);
					String decoded = encryption.BytesToString(bytes);
					System.out.println("Decoded is:\n" + decoded);
					return responseEn;
				}

			} catch (IOException e) {
				e.printStackTrace();
				return "HTTP/1.1 404 Not Found\r\n\r\n<html><body>404 Not Found</body></html>\r\n";
			}
		}
		return null;
	}

	private String GetSecondPageFromServer(String ip, int port)
			throws UnsupportedEncodingException, UnknownHostException,
			IOException {
		String formInputs = "source=server";
		
		String params = URLEncoder.encode("source", "UTF-8") + "="
				+ URLEncoder.encode("server", "UTF-8");
		String hostname = ip;
		
		InetAddress addr = InetAddress.getByName(hostname);
		Socket socket = new Socket(addr, port);
		String path = "/second_page.html";

		BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(socket.getOutputStream(),
						"UTF8"));
		writer.write("POST " + path + " HTTP/1.0\r\n");
		writer.write("Content-Length: " + formInputs.length() + "\r\n");
		writer.write("Content-Type: application/x-www-form-urlencoded\r\n");
		writer.write("\r\n");

		// Send form inputs
		writer.write("source=server");
		writer.flush();

		// Get response
		InputStream inStream = socket.getInputStream();
		byte[] byteBuffer = new byte[10000];
		int count = inStream.read(byteBuffer);
		if(count <= 0)
		{
			System.out.println("Gets no response from the other server.");
			return null;
		}
		
		byte[] responseBytes = new byte[count];
		System.arraycopy(byteBuffer, 0, responseBytes, 0, count);
		
		DesEncryption encryption = new DesEncryption();
		
		byte[] decryptedBytes = encryption.DES_Decrypt(responseBytes, "1234567890");
		
		String out = encryption.BytesToString(decryptedBytes);
		
		//System.out.println("DES decoded is:\n" + out + "\n");
		
		writer.close();
		inStream.close();
		socket.close();
		return out + "\r\n";
	}
}
