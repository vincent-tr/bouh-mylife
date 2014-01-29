var fs = require('fs');
var path = require('path');

var config = require('./config.json');

var data;

var loadData = function() {
	var filename = path.join(config.data.directory, 'data.json');
	var content = fs.readFileSync(filename);
	data = JSON.parse(content);
};

var saveData = function() {
	var filename = path.join(config.data.directory, 'data.json');
	var content = JSON.stringify(data);
	fs.writeFileSync(filename, content);
};

var checkData = function() {
	if(data === null) {
		loadData();
	}
};

var getPlugins = function() {
	checkData();
	return data.plugins;
};

var addPlugin = function(id, config) {
	checkData();
	data.plugins[id] = config;
	saveData();
};
	
var removePlugin = function(id) {
	checkData();
	delete data.plugins[id];
	saveData();
};

var getHardware = function() {
	checkData();
	return data.hardware;
};

var addHardware = function(id, config) {
	checkData();
	data.hardware[id] = config;
	saveData();
};

var removeHardware = function(id) {
	checkData();
	delete data.hardware[id];
	saveData();
};

var getLinks = function() {
	checkData();
	return data.links;
};

var addLink = function(id, config) {
	checkData();
	data.links[id] = config;
	saveData();
};

var removeLink = function(id) {
	checkData();
	delete data.links[id];
	saveData();
};

module.exports.getPlugins = getPlugins;
module.exports.addPlugin = addPlugin;
module.exports.removePlugin = removePlugin;
module.exports.getHardware = getHardware;
module.exports.addHardware = addHardware;
module.exports.removeHardware = removeHardware;
module.exports.getLinks = getLinks;
module.exports.addLink = addLink;
module.exports.removeLink = removeLink;
