package org.mylife.home.raspberry.gpio;

/**
 * Fabrique PWM
 * 
 * @author pumbawoman
 * 
 */
public class PwmAccessFactory extends SysFsAccessFactory {

	private static final PwmAccessFactory instance = new PwmAccessFactory();

	public static final PwmAccessFactory getInstance() {
		return instance;
	}

	private PwmAccessFactory() {
	}

	@Override
	protected void cleanupAccess(SysFsAccess access) {
		((PwmAccess)access).setPulse(0);
	}

	@Override
	protected String getClassPath() {
		return "/sys/class/soft_pwm/";
	}

	@Override
	protected String getDevicePrefix() {
		return "pwm";
	}

	@Override
	protected String getAdminTool() {
		return "/usr/local/bin/pwm-admin";
	}

	@Override
	protected SysFsAccess createAccess(int pin) {
		PwmAccess access = new PwmAccess(pin, this);
		access.setPulse(0);
		return access;
	}

}
