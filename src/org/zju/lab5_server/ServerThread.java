package org.zju.lab5_server;

import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.*;

public class ServerThread extends Thread {

	private Socket socket;

	public ServerThread(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try (OutputStream out = socket.getOutputStream();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));) {
			String request = getRequest(in);
			processRequest(request, out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getRequest(BufferedReader in) throws IOException {
		char[] buffer = new char[10000000];
		int count = in.read(buffer);
		char[] response = new char[count];
		System.arraycopy(buffer, 0, response, 0, count);
		String request = new String(response);
		System.out.println("Received request:\n" + request);
		return request;
	}

	public void processRequest(String input, OutputStream out) {
		// For http protocol details see the http wiki page.
		String[] lines = input.split("[\r\n]+");
		String[] tokens = lines[0].split(" ");

		try (PrintWriter writer = new PrintWriter(new BufferedWriter(
				new OutputStreamWriter(out)))) {
			if (tokens[0].compareTo("GET") == 0
					&& tokens[2].startsWith("HTTP/1.")) {
				String content;
				try {
					String url = tokens[1];
					if (url.compareTo("/") == 0)
						url = "index.html";
					content = new String(Files.readAllBytes(Paths.get(url)));
					writer.print("HTTP/1.1 200 OK\r\n\r\n" + content + "\r\n");
				} catch (IOException e) {
					e.printStackTrace();
					writer.print("HTTP/1.1 404 Not Found\r\n\r\n<html><body>404 Not Found</body></html>\r\n");
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
						if (inputs[1].startsWith("port="))
							port = Integer.parseInt(inputs[1].substring(5));

						byte[] secondPageBytes = GetSecondPageFromServer(ip,
								port);
						if (secondPageBytes == null)
							writer.write("HTTP/1.1 404 Not Found\r\n\r\n<html><body>404 Not Found\nThe other server is either down or the RSA signature is incorrect.</body></html>\r\n");
						else
							out.write(secondPageBytes);
						writer.flush();
					} else if (inputs[0].startsWith("source=server")) {
						// return second page
						String url = "second_page.html";
						byte[] fileBytes = Files.readAllBytes(Paths.get(url));
						// String s = new String(fileBytes, "utf8");
						// new FileWriter("test.txt").write(s);
						DesEncryption encryption = new DesEncryption();
						byte[] httpHeaderBytes = "HTTP/1.1 201 Created\r\n\r\n"
								.getBytes("UTF-8");

						RSA rsa = new RSA();
						int signatureInt = rsa.encoded(123, 7341, 53 * 179); // 123：明文
																				// 7341:私钥
																				// 53*179：大素数乘积
						String signature = "signature="
								+ Integer.toString(signatureInt) + "\r\n";
						byte[] signatureBytes = signature.getBytes();

						byte[] responseBytes = new byte[httpHeaderBytes.length
								+ fileBytes.length];

						System.arraycopy(httpHeaderBytes, 0, responseBytes, 0,
								httpHeaderBytes.length);
						System.arraycopy(fileBytes, 0, responseBytes,
								httpHeaderBytes.length, fileBytes.length);
						byte[] encryptedBytes = encryption.DES_Encrypt(
								responseBytes, "1234567890");

						byte[] sigedBytes = new byte[encryptedBytes.length
								+ signatureBytes.length];
						System.arraycopy(signatureBytes, 0, sigedBytes, 0,
								signatureBytes.length);
						System.arraycopy(encryptedBytes, 0, sigedBytes,
								signatureBytes.length, encryptedBytes.length);

						// bytes = encryption.DES_Decrypt(bytes, "1234567890");
						// String decoded = encryption.BytesToString(bytes);
						BufferedOutputStream stream = new BufferedOutputStream(out);
						stream.write(sigedBytes);
						stream.flush();

					}

				} catch (IOException e) {
					e.printStackTrace();
					writer.print("HTTP/1.1 404 Not Found\r\n\r\n<html><body>Target server Not Found</body></html>\r\n");
				}
			}
		}
	}

	private byte[] GetSecondPageFromServer(String ip, int port)
			throws UnsupportedEncodingException, UnknownHostException,
			IOException {
		String formInputs = "source=server";
		String hostname = ip;

		InetAddress addr = InetAddress.getByName(hostname);
		Socket socket = new Socket(addr, port);
		String path = "/second_page.html";

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				socket.getOutputStream(), "UTF8"));
		writer.write("POST " + path + " HTTP/1.0\r\n");
		writer.write("Content-Length: " + formInputs.length() + "\r\n");
		writer.write("Content-Type: application/x-www-form-urlencoded\r\n");
		writer.write("\r\n");

		// Send form inputs
		writer.write("source=server");
		writer.flush();

		// Get response
		BufferedInputStream inStream = new BufferedInputStream(
				socket.getInputStream());

		byte buffer[] = new byte[100];
		inStream.read(buffer, 0, 16);
		String signatureLine = new String(buffer);
		signatureLine = signatureLine.split("[\\n\\r]+")[0];
		if (signatureLine.startsWith("signature=")) {
			int signatureInt = Integer.parseInt(signatureLine.substring(10));
			RSA rsa = new RSA();
			signatureInt = rsa.encoded(signatureInt, 29, 53 * 179);
			if (signatureInt != 123) {
				socket.close();
				return null;
			}
		} else {
			socket.close();
			return null;
		}

		byte[] byteBuffer = new byte[10000000];
		int count = 0;
		while (true) {
			int thisCount = inStream.read(byteBuffer, count, 100000);
			if (thisCount < 0) {
				break;
			}
			count += thisCount;

		}
		byte[] responseBytes = new byte[count];
		System.arraycopy(byteBuffer, 0, responseBytes, 0, count);

		DesEncryption encryption = new DesEncryption();
		byte[] decryptedBytes = encryption.DES_Decrypt(responseBytes,
				"1234567890");

		writer.close();
		inStream.close();
		socket.close();
		return decryptedBytes;
	}
}
