package mylife.home.hw.driver.layout;

class PinLayoutRev1 extends PinLayoutBase {

	@Override
	protected int pinToGpioOrMinusOne(int pinId) {

		// https://projects.drogon.net/raspberry-pi/wiringpi/pins/

		// on essaye de voir déjà si c'est un pin commun
		int ret = super.pinToGpioOrMinusOne(pinId);
		if (ret != -1)
			return ret;

		switch (pinId) {
		case 3:
			return 0;
		case 5:
			return 1;
		case 13:
			return 21;
		}

		return -1;
	}

}