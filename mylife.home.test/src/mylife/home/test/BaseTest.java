package mylife.home.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.pi4j.io.gpio.*;

public class BaseTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		final GpioPinDigitalOutput pin = Common.getPin();
		
		System.out.println("Write a line to toggle, 'exit' to terminate");
		final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		while(!"exit".equalsIgnoreCase(in.readLine()))
			pin.toggle();
		
		pin.setState(PinState.LOW);
	}

}
