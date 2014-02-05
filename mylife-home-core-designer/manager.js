var request = require('request');
var async = require('async');
var deepEquals = require('deep-equal');

var config = require('./config.json');

var coreUrl = config.core.url;
if (coreUrl.substr(-1) != '/') {
	coreUrl += '/';
}

var data = function(callback) {
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
			fetch(coreUrl + 'api/plugins', cb);
		},
		pluginTypes : function(cb) {
			fetch(coreUrl + 'api/pluginTypes', cb);
		},
		hardware : function(cb) {
			fetch(coreUrl + 'api/hardware', cb);
		},
		links : function(cb) {
			fetch(coreUrl + 'api/links', cb);
		}
	}, callback);
};

var merge = function(newData, callback) {
	data(function(err, ret) {
		if (err) {
			callback(err);
			return;
		}

		doMerge(ret, newData, callback);
	});
};

var doMerge = function(oldData, newData, callback) {

	var mergedPlugins = mergeData(oldData.plugins, newData.plugins);
	var mergedHardware = mergeData(oldData.hardware, newData.hardware);
	var mergedLinks = mergeData(oldData.links, newData.links);

	// TODO : rajouter en destroy/add toutes les links touchées par les
	// plugins/hardware

	return {
		destroy : {
			plugins : mergedPlugins.destroy,
			hardware : mergedHardware.destroy,
			links : mergedLinks.destroy
		},
		create : {
			plugins : mergedPlugins.create,
			hardware : mergedHardware.create,
			links : mergedLinks.create
		}
	};
};

var deepExists = function(array, item) {
	for (var i = 0, l = array.length; i < l; i++) {
		if (deepEquals(array[i], item)) {
			return true;
		}
	}

	return false;
};

var deepWithout = function(array, without) {
	var ret = [];

	for (var i = 0, l = array.length; i < l; i++) {
		var item = array[i];
		if (!deepExists(without, item)) {
			ret.push(item);
		}
	}

	return ret;
};

var mergeData = function(oldArray, newArray) {
	return {
		destroy : deepWithout(oldArray, newArray),
		create : deepWithout(newArray, oldArray)
	};
};

var apply = function(mergeData, callback) {

	var map = {};
	
	destroyArray(map, mergeData.destroy.links, 'links');
	destroyArray(map, mergeData.destroy.plugins, 'plugins');
	destroyArray(map, mergeData.destroy.hardware, 'hardware');
	createArray(map, mergeData.create.plugins, 'plugins');
	createArray(map, mergeData.create.hardware, 'hardware');
	createArray(map, mergeData.create.links, 'links');
	
	async.series(map, callback);
};

var fetchCallback = function(callback) {
	return function(error, response, body) {
		if(error) {
			callback(error);
			return;
		}
		
		if(response.statusCode == 500) {
			callback(body);
			return;
		}
		
		callback(null, body);
	};
};

var destroyArray = function(map, array, type) {
	array.forEach(function(item) {
		map['destroy:' + item.id] = function(callback) {
			var url = coreUrl + 'api/' + type + '/' + encodeURIComponent(item.id);
			console.log('delete %s' + url);
			request.del(url, fetchCallback(callback));
		};
	});
};

var createArray = function(map, array, type) {
	array.forEach(function(item) {
		map['create:' + item.id] = function(callback) {
			var url = coreUrl + 'api/' + type;
			console.log('post %s' + url);
			request.post(url, { json : true, body : item, }, fetchCallback(callback));
		};
	});
};

module.exports.data = data;
module.exports.merge = merge;
module.exports.apply = apply;
