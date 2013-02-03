package mylife.home.hw.driver.layout;

class PinLayoutRev2 extends PinLayoutBase {

	@Override
	protected int pinToGpioOrMinusOne(int pinId) {

		// https://projects.drogon.net/raspberry-pi/wiringpi/pins/

		// on essaye de voir déjà si c'est un pin commun
		int ret = super.pinToGpioOrMinusOne(pinId);
		if (ret != -1)
			return ret;

		switch (pinId) {
		case 3:
			return 2;
		case 5:
			return 3;
		case 13:
			return 27;
		}

		return -1;
	}

}