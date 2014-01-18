package mylife.home.hw.driver.layout;

/**
 * Implémentation de base de PinLayout
 * 
 * @author pumbawoman
 * 
 */
abstract class PinLayoutBase implements PinLayout {

	@Override
	public boolean isValid(int pinId) {
		return pinToGpioOrMinusOne(pinId) != -1;
	}

	@Override
	public int pinToGpio(int pinId) {
		int ret = pinToGpioOrMinusOne(pinId);
		if (ret == -1)
			throw new IllegalArgumentException("Invalid pin number : " + pinId);
		return ret;
	}

	/**
	 * Implémentation de base pour les pins communs à toutes les instances
	 * 
	 * @param pinId
	 * @return
	 */
	protected int pinToGpioOrMinusOne(int pinId) {

		// https://projects.drogon.net/raspberry-pi/wiringpi/pins/

		switch (pinId) {
		case 7:
			return 4;
		case 8:
			return 14;
		case 10:
			return 15;
		case 11:
			return 17;
		case 12:
			return 18;
		case 15:
			return 22;
		case 16:
			return 23;
		case 18:
			return 24;
		case 19:
			return 10;
		case 21:
			return 9;
		case 22:
			return 25;
		case 23:
			return 11;
		case 24:
			return 8;
		case 26:
			return 7;
		}

		return -1;
	}
}
