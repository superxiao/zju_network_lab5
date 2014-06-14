package org.zju.lab5_server;

import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Program {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub		
		
		
		System.out.println("Enter port number the server will use");
		Scanner scanner = new Scanner(System.in);
		int port = scanner.nextInt();
		new HttpServer().startServer(port);
	}

}
