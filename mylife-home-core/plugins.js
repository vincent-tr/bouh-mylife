var netobject = require('./netobject.js');

var api = {
	netobject : netobject,
};

var pluginTypes = {};
var pluginContainers = {};

var loadPlugin = function(name) {
	var pluginType = pluginTypes[name];
	if (pluginType === undefined) {
		var plugin = require('./plugins/' + name + '.js');
		var clazz = plugin.init(api);
		pluginType = {
			plugin : plugin,
			clazz : clazz,
			ui : plugin.ui,
			create : plugin.create
		};
		pluginTypes[name] = pluginType;
	}

	return pluginType;
};

var create = function(config) {

	var id = config.id;
	
	if(pluginContainers[id] !== undefined) {
		throw new Error('container already exists');
	}
	
	var type = config.type;
	var pluginType = loadPlugin(type);

	var object = netobject.netObject(id, pluginType.clazz);

	var context = {
		object : object,
		parameters : config.parameters
	};

	var pluginInstance = pluginType.create(context);

	var channels = [ '#mylife-home-core' ];
	if (pluginType.ui) {
		channels.push('#mylife-home-ui');
	}

	var netContainer = netobject.publish(object, channels, true);

	var container = {
		id : id,
		config: config,
		pluginType : pluginType,
		pluginInstance : pluginInstance,
		netContainer : netContainer
	};
	
	pluginContainers[container.id] = container;
	return container;
};

var destroy = function(id) {
	var container = pluginContainers[id];
	if(container === undefined) {
		return false;
	}
	
	var destroy = container.destroy();
	if (typeof (destroy) === 'function') {
		destroy();
	}

	netobject.unpublish(container.netContainer);
	delete pluginContainers[id];
	return true;
};

var list = function() {
	return pluginContainers;
};

module.exports.create = create;
module.exports.destroy = destroy;
module.exports.list = list;
