package org.zju.lab5_server;

import java.net.*;
import java.nio.CharBuffer;
import java.io.*;

public class ServerThread extends Thread {

	private Socket socket;

	public ServerThread(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));) {
			HttpProtocol protocol = new HttpProtocol();
			
			CharBuffer buffer = CharBuffer.allocate(1000);
			int offset = in.read(buffer);
			buffer.position(0);
			String request = buffer.subSequence(0, offset).toString();
			System.out.println("Request is:\n" + request);
			String response = protocol.processInput(request);
			out.print(response);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
