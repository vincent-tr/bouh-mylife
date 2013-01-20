package mylife.home.test;

import com.pi4j.wiringpi.SoftPwm;

public class SoftPwmTest {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
        // initialize wiringPi library
        com.pi4j.wiringpi.Gpio.wiringPiSetup();

        // create soft-pwm pins (min=0 ; max=100)
        if(SoftPwm.softPwmCreate(6, 0, 100) != 0) {
        	System.out.println("erreur");
        	return;
        }

        // continuous loop
        while (true)
        {
            // fade LED to fully ON
            for (int i = 0; i <= 100; i++)
            {
                SoftPwm.softPwmWrite(6, i);
                Thread.sleep(100);
            }

            // fade LED to fully OFF
            for (int i = 100; i >= 0; i--)
            {
                SoftPwm.softPwmWrite(6, i);
                Thread.sleep(100);
            }
        }
	}

}
