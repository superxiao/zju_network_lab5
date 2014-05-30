package org.zju.lab5_server;

import java.io.*;
import java.net.*;

public class HttpServer {
	public void startServer(int portNum) {
		try (ServerSocket serverSocket = new ServerSocket(portNum);) {
			while (true) {
				System.out.println("Waiting for a incoming connection...");
				new ServerThread(serverSocket.accept()).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
