/**
 * New node file
 */

'use strict';

var module = angular.module('mylife.api', ['ngResource']);

module.factory('api', ['$resource', function($resource) {
	return {
		data:  $resource('/data', {}, {
			get: { method: 'GET' }
		}),
		updateHardware: $resource('/updateHardware', {}, {
			post: { method: 'POST' }
		}),
		merge: $resource('/merge', {}, {
			post: { method: 'POST' }
		}),
		apply: $resource('/apply', {}, {
			post: { method: 'POST' }
		})
	};
}]);

module.factory('dataAccess', ['api', function(api) {
	
	var prepareData = function(data) {
		
		var checkDesignerData = function(item) {
			var designer = item.designer;
			if(!designer) {
				item.designer = designer = {
					x: 0,
					y: 0
				};
			}
		};

		var attachInternal = function(item) {
			if(!item.internal) {
				item.internal = {};
			}
		};
		
		var attachTypeToPlugin = function(plugin) {
			
			for (var i = 0, l = data.pluginTypes.length; i < l; i++) {
				var type = data.pluginTypes[i];
				if (type.id === plugin.type) {
					plugin.internal.type = type;
					break;
				}
			}
		};
	
		data.pluginTypes.forEach(attachInternal);
		data.plugins.forEach(attachInternal);
		data.hardware.forEach(attachInternal);
		data.links.forEach(attachInternal);
		
		data.plugins.forEach(attachTypeToPlugin);
		
		data.plugins.forEach(checkDesignerData);
		data.hardware.forEach(checkDesignerData);
	};
	
	var load = function(callback) {
		api.data.get({}, function(data) {
			prepareData(data);
			if(callback) {
				callback(data);
			}
		});
	};
	
	var save = function(data, callback) {
		
		var removeInternal = function(item) {
			if(item.internal) {
				delete item.internal;
			}
		};
		
		var clone = function(obj) {
			return JSON.parse(JSON.stringify(obj));
		};
		
		var prepareArray = function(source) {
			var dest = clone(source);
			dest.forEach(removeInternal);
		};
		
		var sendData = {
			plugins: prepareArray(data.plugins),
			hardware: prepareArray(data.hardware),
			links: prepareArray(data.links)
		};
		
		api.merge.post({}, sendData, function(res) {
			// TODO
			callback();
		});
	};
	
	var loadHardware = function(data, url) {
		// TODO
	};
	
	return {
		load: load,
		save: save,
		loadHardware: loadHardware
	};
	
}]);
