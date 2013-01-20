package mylife.home.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class PulseTest implements Runnable {

	private boolean exit;
	private Thread thread;
	
	private void startPulse() {
		thread = new Thread(this);
		thread.start();
	}
	
	private void stopPulse() throws InterruptedException {
		exit = true;
		thread.join();
	}
	
	public void run() {
		int count = 0;
		while(!exit) {
			if(count == 5)
				count = 0;
			
			if(count == 0)
				pin.pulse(1, true);
			else
				useless.pulse(1, true);
			
			++count;
		}
	}
	
	GpioPinDigitalOutput pin;
	GpioPinDigitalOutput useless;
	
	private void execute() throws IOException, InterruptedException {
		
		pin = Common.getPin();
		final GpioController controller = GpioFactory.getInstance();
		useless = controller.provisionDigitalOutputPin(RaspiPin.GPIO_12, PinState.LOW);
		
		startPulse();
		
		System.out.println("Light up, write a line to shut down and exit");
		final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		in.readLine();
		
		stopPulse();
		pin.setState(PinState.LOW);
		useless.setState(PinState.LOW);
		
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		new PulseTest().execute();
	}
}
