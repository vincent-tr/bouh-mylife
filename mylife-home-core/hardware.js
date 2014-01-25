var netobject = require('./netobject.js');
var config = require('./config.json');

var hardware = [];

var create = function(config) {
	var id = config.id;
	var clazz = config.class;
	var object = netobject.netObject(id, clazz);
	var container = netobject.publish(object, [ '#mylife-home-hardware' ], false);

	return {
		id : id,
		clazz : clazz,
		object : object,
		container : container,
		destroy : function() {
			netobject.unpublish(container);
		}
	};
};

var initialize = function() {
	var hardwareConfig = config.hardware;
	for (var i = 0, l = hardwareConfig.length; i < l; i++) {
		var configItem = hardwareConfig[i];
		var container = create(configItem);
		hardware.push(container);
	}
};

var terminate = function() {
	for (var i = 0, l = hardware.length; i < l; i++) {
		var item = hardware[i];
		item.destroy();
	}
	hardware.length = 0;
};

module.exports.initialize = initialize;
module.exports.terminate = terminate;
