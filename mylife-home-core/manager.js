var assert = require('assert');
var events = require('events');
var util = require('util');

var plugins = require('./plugins.js');
var hardware = require('./hardware.js');
var links = require('./links.js');
var data = require('./data.js');

var initialize = function() {

	var hardwareData = data.getHardware();
	for ( var hid in hardwareData) {
		if (hardwareData.hasOwnProperty(hid)) {
			hardware.create(hardwareData[hid]);
		}
	}

	var pluginsData = data.getPlugins();
	for ( var pid in pluginsData) {
		if (pluginsData.hasOwnProperty(pid)) {
			plugins.create(pluginsData[pid]);
		}
	}

	var linksData = data.getLinks();
	for ( var lid in linksData) {
		if (linksData.hasOwnProperty(lid)) {
			links.create(linksData[lid]);
		}
	}
};

var terminate = function() {
	
	var runningLinks = links.list();
	for(var lid in runningLinks) {
		if (runningLinks.hasOwnProperty(lid)) {
			links.destroy(lid);
		}
	}

	var runningPlugins = plugins.list();
	for ( var pid in runningPlugins) {
		if (runningPlugins.hasOwnProperty(pid)) {
			plugins.destroy(runningPlugins[pid]);
		}
	}

	var runningHardware = hardware.list();
	for ( var hid in runningHardware) {
		if (runningHardware.hasOwnProperty(hid)) {
			hardware.destroy(runningHardware[hid]);
		}
	}
};

var checkLink = function(id) {
	var runningLinks = links.list();
	for(var linkId in runningLinks) {
		if (runningLinks.hasOwnProperty(linkId)) {
			var link = runningLinks[linkId];
			var linkConfig = link.config;
			if(linkConfig.sourceComponent === id || linkConfig.destinationComponent === id) {
				throw new Error(util.format('object %s used in link %s', id, linkId));
			}
		}
	}
};

var getPlugins = function() {
	return plugins.list();
};

var addPlugin = function(config) {
	var plugin = plugins.create(config);
	data.addPlugin(plugin.id, config);
	return plugin.id;
};
	
var removePlugin = function(id) {
	// on regarde s'il n'est pas utilis� dans des liens
	checkLink(id);
	
	if(!plugins.destroy(id)) {
		throw new Error('plugin not found');
	}
	
	data.removePlugin(id);
};

var getHardware = function() {
	return hardware.list();
};

var addHardware = function(config) {
	var hw = hardware.create(config);
	data.addHardware(hw.id, config);
	return hw.id;
};

var removeHardware = function(id) {
	// on regarde s'il n'est pas utilis� dans des liens
	checkLink(id);
	
	if(!hardware.destroy(id)) {
		throw new Error('hardware not found');
	}
	
	data.removeHardware(id);
};

var getLinks = function() {
	return links.list();
};

var addLink = function(config) {
	var link = links.create(config);
	data.addLink(link.id, config);
	return link.id;
};

var removeLink = function(id) {
	if(!links.destroy(id)) {
		throw new Error('link not found');
	}
	
	data.removeLink(id);
};

module.exports.initialize = initialize;
module.exports.terminate = terminate;
module.exports.getPlugins = getPlugins;
module.exports.addPlugin = addPlugin;
module.exports.removePlugin = removePlugin;
module.exports.getHardware = getHardware;
module.exports.addHardware = addHardware;
module.exports.removeHardware = removeHardware;
module.exports.getLinks = getLinks;
module.exports.addLink = addLink;
module.exports.removeLink = removeLink;
