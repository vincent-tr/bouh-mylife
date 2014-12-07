package org.mylife.home.raspberry.gpio;

/**
 * Fabrique GPIO
 * @author pumbawoman
 *
 */
public class GpioAccessFactory extends SysFsAccessFactory {

	private static final GpioAccessFactory instance = new GpioAccessFactory();

	public static final GpioAccessFactory getInstance() {
		return instance;
	}

	private GpioAccessFactory() {
	}

	@Override
	protected void cleanupAccess(SysFsAccess access) {
		((GpioAccess)access).terminate();
	}

	@Override
	protected String getClassPath() {
		return "/sys/class/gpio/";
	}

	@Override
	protected String getDevicePrefix() {
		return "gpio";
	}

	@Override
	protected String getAdminTool() {
		return "/usr/local/bin/gpio-admin";
	}

	@Override
	protected SysFsAccess createAccess(int pin) {
		GpioAccess access = new GpioAccess(pin, this);
		access.init();
		return access;
	}
}
