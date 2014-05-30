package org.zju.lab5_server;

import java.net.*;
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
			String inputLine, outputLine;
			HttpProtocol protocol = new HttpProtocol();
			while ((inputLine = in.readLine()) != null) {
				outputLine = protocol.processInput(inputLine);
				if(outputLine == null)
					break;
				out.println(outputLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
