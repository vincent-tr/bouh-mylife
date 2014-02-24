var fs = require('fs');
var path = require('path');

var config = require('./config.json');

var data;
var uiData;

var getFilename = function(name) {
	var filePath = path.join(config.data.directory, name);
	var filename = path.resolve(__dirname, filePath);
	return filename;
};

var dataFilename = function() {
	var filename = getFilename('data.json');
	console.info('using data file : %s', filename);
	return filename;
};

var uiFilename = function() {
	var filename = getFilename('ui.json');
	console.info('using ui file : %s', filename);
	return filename;
};

var loadData = function() {
	var content = fs.readFileSync(dataFilename());
	data = JSON.parse(content);
};

var saveData = function() {
	var content = JSON.stringify(data);
	fs.writeFileSync(dataFilename(), content);
};

var checkData = function() {
	if(!data) {
		loadData();
	}
};

var loadUiData = function() {
	var content = fs.readFileSync(uiFilename());
	uiData = JSON.parse(content);
};

var saveUiData = function() {
	var content = JSON.stringify(uiData);
	fs.writeFileSync(uiFilename(), content);
};

var checkUiData = function() {
	if(uiData === null) {
		loadUiData();
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

var getUi = function() {
	checkUiData();
	return uiData;
};

var setUi = function(data) {
	uiData = data;
	saveUiData();
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
module.exports.getUi = getUi;
module.exports.setUi = setUi;
