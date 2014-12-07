/*
 * core.c
 *
 *  Created on: 22 sept. 2013
 *      Author: pumbawoman
 */

/*
 * Copyright 2012, Quick2Wire.com.
 * Dual licensed under MIT or LGPL licenses: http://license.quick2wire.com
 */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <error.h>
#include <errno.h>
#include <limits.h>
#include <stdint.h>
#include <sys/mman.h>

/*
#define GPIO_CLASS_PATH "/sys/class/gpio/"
#define GPIO_DEVICE_PREFIX "gpio"
#define GPIO_DEVICE_FILES "direction", "value", "active_low", "edge"
#define GPIO_EXPORT_PATH (GPIO_CLASS_PATH "export")
#define GPIO_UNEXPORT_PATH (GPIO_CLASS_PATH "unexport")

#define GPIO_ALLOW_PULL
*/

static void usage_error(char **argv) {
  fprintf(stderr, "usage: %s {export|unexport} <gpio-pin>"
#ifdef GPIO_ALLOW_PULL
		  " [pullup|pulldown]"
#endif // GPIO_ALLOW_PULL
		  "\n", argv[0]);
  exit(1);
}

static void allow_access_by_user(unsigned int pin, const char *filename) {
  char path[PATH_MAX];
  int size = snprintf(path, PATH_MAX, GPIO_CLASS_PATH GPIO_DEVICE_PREFIX "%u/%s", pin, filename);

  if (size >= PATH_MAX) {
    error(7, 0, "path of GPIO pin is too long!");
  }

  if (chmod(path, S_IRUSR|S_IWUSR|S_IRGRP|S_IWGRP|S_IROTH|S_IWOTH) != 0) {
    error(6, errno, "failed to set permissions of %s", path);
  }
}

static unsigned int parse_gpio_pin(const char *pin_str) {
  char *endp;
  unsigned int pin;

  if (pin_str[0] == '\0') {
    error(2, 0, "empty string given for GPIO pin number");
  }

  pin = strtoul(pin_str, &endp, 0);

  if (*endp != '\0') {
    error(2, 0, "%s is not a valid GPIO pin number", pin_str);
  }

  return pin;
}

static void write_pin_to_path(const char *path, unsigned int pin) {
  FILE * out = fopen(path, "w");

  if (out == NULL) {
    error(3, errno, "could not open %s", path);
  }

  if (fprintf(out, "%u\n", pin) < 0) {
    error(4, errno, "could not write GPIO pin number to %s", path);
  }

  if (fclose(out) == EOF) {
    error(4, errno, "could not flush data to %s", path);
  }
}

#ifdef GPIO_ALLOW_PULL

#define DOWN    1
#define UP      2
#define BLOCK_SIZE 4096
#define GPIO_BASE 0x20200000
#define GPPUD       37
#define GPPUDCLK0   38
#define BIT(x)  (1 << ((x) & 31))
#define BANK(x) (((x) & 32) >> 5)

static void set_pullupdown(int pin, int direction) {
    volatile uint32_t* gpio;
    int fd = open("/dev/mem", O_RDWR|O_SYNC);
    if (fd < 0) {
        fprintf(stderr, "could not open /dev/mem: %s\n", strerror(errno));
        exit(-1);
    }

    gpio = (volatile uint32_t*) mmap(NULL, BLOCK_SIZE, PROT_READ|PROT_WRITE, MAP_SHARED, fd, GPIO_BASE);
    if (gpio == MAP_FAILED) {
        fprintf(stderr, "could not map GPIO region: %s\n", strerror(errno));
        close(fd);
        exit(-1);
    }

    // set the direction
    gpio[GPPUD] = direction;
    usleep(100);

    // clock it in to the single pin
    gpio[GPPUDCLK0+BANK(pin)] = BIT(pin);
    usleep(100);

    gpio[GPPUD] = 0;
    gpio[GPPUDCLK0+BANK(pin)] = 0;

    munmap((char*)gpio, BLOCK_SIZE);
    close(fd);
}

#endif // GPIO_ALLOW_PULL

int main(int argc, char **argv) {
#ifdef GPIO_ALLOW_PULL
	int pullupdown = -1;
#endif // GPIO_ALLOW_PULL
	char *gpio_files[] = { GPIO_DEVICE_FILES };
	int i;

  if (argc != 3
#ifdef GPIO_ALLOW_PULL
		  && argc != 4
#endif // GPIO_ALLOW_PULL
		  ) {
    usage_error(argv);
  }

  const char *command = argv[1];
  const char *pin_str = argv[2];

  unsigned int pin = parse_gpio_pin(pin_str);

#ifdef GPIO_ALLOW_PULL
  if (argc == 4) {
    if (strcmp("pullup", argv[3]) == 0)
      pullupdown = UP;
    else if (strcmp("pulldown", argv[3]) == 0)
      pullupdown = DOWN;
    else {
      usage_error(argv);
    }
  }
#endif // GPIO_ALLOW_PULL

  if (strcmp(command, "export") == 0) {
    write_pin_to_path(GPIO_EXPORT_PATH, pin);

    for(i=0; i<(sizeof(gpio_files) / sizeof(*gpio_files)); i++)
    	allow_access_by_user(pin, gpio_files[i]);

#ifdef GPIO_ALLOW_PULL
    if (pullupdown != -1) {
      set_pullupdown(pin, pullupdown);
    }
#endif // GPIO_ALLOW_PULL
  }
  else if (strcmp(command, "unexport") == 0) {
    write_pin_to_path(GPIO_UNEXPORT_PATH, pin);
  }
  else {
    usage_error(argv);
  }

  return 0;
}
