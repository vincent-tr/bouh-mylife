var request = require('request');
var async = require('async');
var deepEquals = require('deep-equal');

var utils = require('./utils.js');

var data = function(callback) {
	var fetch = function(url, callback) {
		request.get(url, function(err, response, body) {
			if (err) {
				callback(JSON.parse(err));
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
		},
		hardware : function(cb) {
			fetch(utils.coreUrl + 'api/hardware', cb);
		},
		links : function(cb) {
			fetch(utils.coreUrl + 'api/links', cb);
		}
	}, callback);
};

var hardware = function(url, callback) {
	request.post(url, function(err, response, body) {
		if (err) {
			callback(err);
		} else {
			callback(null, JSON.parse(body));
		}
	});
};

var merge = function(newData, callback) {
	data(function(err, ret) {
		if (err) {
			callback(err);
			return;
		}

		var mergedData = doMerge(ret, newData, callback);
		callback(null, mergedData);
	});
};

var doMerge = function(oldData, newData, callback) {

	var mergedPlugins = mergeData(oldData.plugins, newData.plugins);
	var mergedHardware = mergeData(oldData.hardware, newData.hardware);
	var mergedLinks = mergeData(oldData.links, newData.links);

	// on cherche les links des oldData qui sont rï¿½fï¿½rencï¿½s dans les delete et qui ne sont pas dans les links a delete
	// on les delete et recrï¿½e
	oldData.links.forEach(function(link) {
		var recreateLink = function(link) {
			mergedLinks.destroy.push(link);
			mergedLinks.create.push(link);
		};
		
		if(findById(mergedLinks.destroy, link.id)) {
			// dï¿½jï¿½ prï¿½sent rien ï¿½ faire
			return;
		}
		
		if(findById(mergedHardware.destroy, link.sourceComponent)) {
			recreateLink(link);
			return;
		}
		
		if(findById(mergedPlugins.destroy, link.destinationComponent)) {
			recreateLink(link);
			return;
		}

		if(findById(mergedHardware.destroy, link.sourceComponent)) {
			recreateLink(link);
			return;
		}
		
		if(findById(mergedPlugins.destroy, link.destinationComponent)) {
			recreateLink(link);
			return;
		}
	});

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

var findById = function(array, id) {
	for (var i = 0, l = array.length; i < l; i++) {
		var item = array[i];
		if(item.id === id) {
			return item;
		}
	}
	
	return undefined;
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
			var url = utils.coreUrl + 'api/' + type + '/' + encodeURIComponent(item.id);
			console.log('delete ' + url);
			request.del(url, fetchCallback(callback));
		};
	});
};

var createArray = function(map, array, type) {
	array.forEach(function(item) {
		map['create:' + item.id] = function(callback) {
			var url = utils.coreUrl + 'api/' + type;
			console.log('post ' + url);
			request.post(url, { json : true, body : item, }, fetchCallback(callback));
		};
	});
};

module.exports.data = data;
module.exports.hardware = hardware;
module.exports.merge = merge;
module.exports.apply = apply;
