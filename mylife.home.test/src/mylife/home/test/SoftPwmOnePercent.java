package mylife.home.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.pi4j.wiringpi.SoftPwm;

public class SoftPwmOnePercent {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
        // initialize wiringPi library
        com.pi4j.wiringpi.Gpio.wiringPiSetup();

        // create soft-pwm pins (min=0 ; max=100)
        if(SoftPwm.softPwmCreate(6, 0, 8) != 0) {
        	System.out.println("erreur");
        	return;
        }

        SoftPwm.softPwmWrite(6, 4);
        
		System.out.println("Light up, write a line to shut down and exit");
		final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		
		in.readLine();
	}

}
