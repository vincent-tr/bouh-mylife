var request = require('request');
var async = require('async');

var utils = require('./utils.js');

var get = function(callback) {
	var url = utils.coreUrl + 'api/ui';
	request.get(url, function(err, response, body) {
		if (err) {
			callback(err);
		} else {
			callback(null, JSON.parse(body));
		}
	});
};

var set = function(data, callback) {
	var url = utils.coreUrl + 'api/ui';
	console.log('post ' + url);
	request.post(url, { json : true, body : data, }, 
			function(err, response, body) {
		if (err) {
			callback(err);
		} else {
			callback(null, body);
		}
	});
};

var components = function(callback) {

	var fetch = function(url, callback) {
		request.get(url, function(err, response, body) {
			if (err) {
				callback(err);
			} else {
				callback(null, JSON.parse(body));
			}
		});
	};

	async.parallel({
		plugins : function(cb) {
			fetch(utils.coreUrl + 'api/plugins', cb);
		},
		pluginTypes : function(cb) {
			fetch(utils.coreUrl + 'api/pluginTypes', cb);
		}
	}, function(err, data) {
		if(err) {
			callback(err);
			return;
		}
		
		// on ne garde que les plugins et les types ui
		utils.arrayKeepIf(data.pluginTypes, function(type) {
			return type.ui;
		});
		
		utils.arrayKeepIf(data.plugins, function(plugin) {
			for(var i=0, l=data.pluginTypes.length; i<l; i++) {
				if(plugin.type === data.pluginTypes[i].id) {
					return true;
				}
			}
			return false;
		});
		
		callback(null, data);
	});
};

module.exports.get = get;
module.exports.set = set;
module.exports.components = components;
