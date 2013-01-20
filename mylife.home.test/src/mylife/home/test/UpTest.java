package mylife.home.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;

public class UpTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		final GpioPinDigitalOutput pin = Common.getPin();
		
		pin.setState(PinState.HIGH);
		
		System.out.println("Light up, write a line to shut down and exit");
		final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		in.readLine();
		
		pin.setState(PinState.LOW);
	}

}
