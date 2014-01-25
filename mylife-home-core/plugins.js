var netobject = require('./netobject.js');
var config = require('./config.json');

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

	return {
		id : id,
		pluginType : pluginType,
		pluginInstance : pluginInstance,
		netContainer : netContainer
	};
};

var destroy = function(container) {
	var destroy = container.destroy();
	if (typeof (destroy) === 'function') {
		destroy();
	}

	netobject.unpublish(container.netContainer);
};

var initialize = function() {
	var components = config.components;
	for (var i = 0, l = components.length; i < l; i++) {
		var component = components[i];
		var container = create(component);
		pluginContainers[container.id] = container;
	}
};

var terminate = function() {
	for ( var id in pluginContainers) {
		if (pluginContainers.hasOwnProperty(id)) {

			var container = pluginContainers[id];
			delete pluginContainers[id];
			destroy(container);
		}
	}
};

module.exports.initialize = initialize;
module.exports.terminate = terminate;
