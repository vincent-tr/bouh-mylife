CC=gcc
CFLAGS=-Wall -O2

GPIODEFS=-DGPIO_CLASS_PATH=\"/sys/class/gpio/\" -DGPIO_DEVICE_PREFIX=\"gpio\" -DGPIO_DEVICE_FILES=\"direction\",\ \"value\",\ \"active_low\",\ \"edge\" -DGPIO_EXPORT_PATH=\(GPIO_CLASS_PATH\ \"export\"\) -DGPIO_UNEXPORT_PATH=\(GPIO_CLASS_PATH\ \"unexport\"\) -DGPIO_ALLOW_PULL
PWMDEFS=-DGPIO_CLASS_PATH=\"/sys/class/soft_pwm/\" -DGPIO_DEVICE_PREFIX=\"pwm\" -DGPIO_DEVICE_FILES=\"period\",\ \"pulse\",\ \"pulses\" -DGPIO_EXPORT_PATH=\(GPIO_CLASS_PATH\ \"export\"\) -DGPIO_UNEXPORT_PATH=\(GPIO_CLASS_PATH\ \"unexport\"\)

ifndef DESTDIR
DESTDIR=/usr/local
endif

all: gpio-admin pwm-admin

gpio-admin:
	$(CC) $(CFLAGS) $(GPIODEFS) core.c -o gpio-admin

pwm-admin:
	$(CC) $(CFLAGS) $(PWMDEFS) core.c -o pwm-admin

clean:
	rm -rf gpio-admin
	rm -rf pwm-admin

install: all
	mkdir -p $(DESTDIR)/bin/
	install gpio-admin $(DESTDIR)/bin/
	install pwm-admin $(DESTDIR)/bin/
	chmod u=rwxs,g=rx,o=rx $(DESTDIR)/bin/gpio-admin
	chmod u=rwxs,g=rx,o=rx $(DESTDIR)/bin/pwm-admin

uninstall:
	rm -rf $(DESTDIR)/bin/gpio-admin
	rm -rf $(DESTDIR)/bin/pwm-admin
