package mylife.home.irc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import mylife.home.irc.server.Configuration;
import mylife.home.irc.server.IrcServer;

/**
 * Hello world!
 * 
 */
public class App {
	public static void main(String[] args) {
		try {
			
			System.out.println("Starting IrcServer");

			Configuration config = new Configuration();
			config.setName("test.irc.mti-team2.dyndns.org");
			config.setToken(1);
			config.setInfo("Test server");
			config.setListenPort(6667);
			config.setRecvBufferSize(1024);
			config.setSendBufferSize(1024 * 64);
			config.getCommands(); // TODO
			IrcServer server = new IrcServer();
			server.open(config);

			System.out.println("Started");

			readLine();
			
			System.out.println("Stopping");
			
			server.close();

			System.out.println("Stopped");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String readLine() throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		return br.readLine();
	}
}
