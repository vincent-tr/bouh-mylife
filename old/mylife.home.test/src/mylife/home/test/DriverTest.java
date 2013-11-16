package mylife.home.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.EnumSet;

import mylife.home.hw.api.DigitalOutputDevice;
import mylife.home.hw.api.Options;
import mylife.home.hw.driver.DeviceManagerService;
import mylife.home.hw.driver.device.PollingService;


public class DriverTest {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		PollingService.getInstance().start();
		
		DeviceManagerService service = new DeviceManagerService();
		DigitalOutputDevice device = (DigitalOutputDevice)service.open(22, EnumSet.of(Options.DIRECTION_OUTPUT, Options.TYPE_DIGITAL));

		System.out.println("Write a line to toggle, 'exit' to terminate");
		final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		while(!"exit".equalsIgnoreCase(in.readLine()))
			device.setValue(!device.getValue());
		
		device.close();
		
		PollingService.getInstance().stop();
	}

}
