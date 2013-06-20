/*
 * config_base.h
 *
 *  Created on: 18 juin 2013
 *      Author: pumbawoman
 */

#ifndef CONFIG_BASE_H_
#define CONFIG_BASE_H_

#define CONFIG_BASE_DIRECTORY "/home/pi/mylife-home-embedded"
#define CONFIG_BINARY "core"
#define CONFIG_MODULES_DIRECTORY CONFIG_BASE_DIRECTORY "/modules"
#define CONFIG_PID_FILE "/var/run/mylife-home-embedded-core.pid"

#define CONFIG_LOOP_MS 10

#define CONFIG_IRC_SERVER "127.0.0.1"
#define CONFIG_IRC_PORT 6667
#define CONFIG_IRC_CHANNEL "#mylife-home"

#endif /* CONFIG_BASE_H_ */
