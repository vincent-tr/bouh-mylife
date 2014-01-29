var netobject = require('./netobject.js');

var hardware = {};

var create = function(config) {
	var id = config.id;
	
	if(hardware[id] !== undefined) {
		throw new Error('hardware already exists');
	}
	
	var clazz = config.class;
	var object = netobject.netObject(id, clazz);
	var container = netobject.publish(object, [ '#mylife-home-hardware' ], false);

	var item = {
		id : id,
		clazz : clazz,
		object : object,
		container : container,
		destroy : function() {
			netobject.unpublish(container);
		}
	};
	
	hardware[id] = item;
	return item;
};

var destroy = function(id) {
	var item = hardware[id];
	if(item === undefined) {
		return false;
	}
	
	item.destroy();
	delete hardware[id]
	return true;
};

module.exports.create = create;
module.exports.destroy = destroy;
