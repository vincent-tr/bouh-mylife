var fs = require('fs');
var path = require('path');

var netobject = require('./netobject.js');

var api = {
	netobject : netobject,
};

var pluginTypes;
var pluginContainers = {};

var refreshPluginTypes = function() {
	var directory = path.join(__dirname, 'plugins');
	var files = fs.readdirSync(directory);
	for (var i = 0, l = files.length; i < l; i++) {
		var file = path.join(directory, files[i]);
		if (path.extname(file) !== '.js') {
			continue;
		}

		var name = path.basename(file, path.extname(file));
		if (pluginTypes[name]) {
			// already exists
			continue;
		}

		var plugin = require(file);
		var initData = plugin.init(api);
		var pluginType = {
			id : name,
			plugin : plugin,
			'class' : initData['class'],
			displayName : initData.displayName,
			imageUrl : initData.imageUrl,
			arguments : initData.arguments,
			ui : plugin.ui,
			create : plugin.create
		};
		pluginTypes[name] = pluginType;
	}
};

var checkPluginTypes = function() {
	if (!pluginTypes) {
		pluginTypes = {};
		refreshPluginTypes();
	}
};

var create = function(config) {

	var id = config.id;

	if (pluginContainers[id] !== undefined) {
		throw new Error('container already exists');
	}

	var type = config.type;
	checkPluginTypes();
	var pluginType = pluginTypes[type];

	var object = netobject.netObject(id, pluginType['class']);

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
		typeId : pluginType.id,
		config : config,
		pluginType : pluginType,
		pluginInstance : pluginInstance,
		netContainer : netContainer
	};

	pluginContainers[container.id] = container;
	return container;
};

var destroy = function(id) {
	var container = pluginContainers[id];
	if (container === undefined) {
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

var register = function(name, content) {
	checkPluginTypes();

	// check if exists
	if (pluginTypes[name]) {
		throw new Error('A plugin with same name already exists');
	}

	var filename = path.join(__dirname, 'plugins', name + '.js');
	fs.writeFileSync(filename, content);

	refreshPluginTypes();
};

module.exports.create = create;
module.exports.destroy = destroy;
module.exports.list = list;
module.exports.types = types;
module.exports.register = register;
