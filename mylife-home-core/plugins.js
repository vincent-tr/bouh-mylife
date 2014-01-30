var fs = require('fs');
var path = require('path');

var netobject = require('./netobject.js');

var api = {
	netobject : netobject,
};

var pluginTypes;
var pluginContainers = {};

var loadPluginTypes = function() {
	var plugins = {};
	var directory = path.join(__dirname, 'plugins');
	var files = fs.readdirSync(directory);
	for(var i=0, l=files.length; i<l; i++) {
		var file = path.join(directory, files[i]);
		if(path.extname(file) !== '.js') {
			continue;
		}
		
		var name = path.basename(file, path.extname(file));
		var plugin = require(file);
		var initData = plugin.init(api);
		var pluginType = {
			plugin : plugin,
			clazz : initData.clazz,
			displayName : initData.displayName,
			arguments : initData.arguments,
			ui : plugin.ui,
			create : plugin.create
		};
		plugins[name] = pluginType;
	}
	return plugins;
};

var checkPluginTypes = function() {
	if(!pluginTypes) {
		pluginTypes = loadPluginTypes();
	}
};

var create = function(config) {

	var id = config.id;
	
	if(pluginContainers[id] !== undefined) {
		throw new Error('container already exists');
	}
	
	var type = config.type;
	checkPluginTypes();
	var pluginType = pluginTypes[type];

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
	
	var destroy = container.pluginInstance.destroy;
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

var types = function() {
	checkPluginTypes();
	return pluginTypes;
};

module.exports.create = create;
module.exports.destroy = destroy;
module.exports.list = list;
module.exports.types = types;
