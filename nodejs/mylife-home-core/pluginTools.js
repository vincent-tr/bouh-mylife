
var path = require('path');
var fs = require('fs');
//var Buffer = require('buffer');
var util = require('util');

var mime = require('mime');

var loadIcon = function(file) {
	var buffer = fs.readFileSync(file);
	var content = buffer.toString('base64');
	var type = mime.lookup(file);
	return util.format('data:%s;base64,%s', type, content);
};

var loadDefaultIcon = function(pluginFilename, ext) {
	if(!ext) {
		ext = 'png';
	}
	if(ext[0] !== '.') {
		ext = '.' + ext;
	}
	
	var file = pluginFilename.substr(0, pluginFilename.lastIndexOf('.')) + ext;
	return loadIcon(file);
};

module.exports.loadIcon = loadIcon;
module.exports.loadDefaultIcon = loadDefaultIcon;