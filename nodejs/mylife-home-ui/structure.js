/**
 * Publication de la structure
 */

var request = require('request');
var async = require('async');

var utils = require('./utils.js');

var get = function(callback) {

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
		ui : function(cb) {
			fetch(utils.coreUrl + 'api/ui', cb);
		},
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
		
		var getComponentPlugin = function(componentId) {
			for(var i=0, l=data.plugins.length; i<l; i++) {
				if(componentId === data.plugins[i].id) {
					return data.plugins[i];
				}
			}
			return null;
		};
		
		var getPluginType = function(plugin) {
			for(var i=0, l=data.pluginTypes.length; i<l; i++) {
				if(plugin.type === data.pluginTypes[i].id) {
					return data.pluginTypes[i];
				}
			}
			return null;
		};
		
		var getAttributeIndex = function(componentId, attributeName) {
			var plugin = getComponentPlugin(componentId);
			if(!plugin) {
				return null;
			}
			
			var type = getPluginType(plugin);
			if(!type) {
				return null;
			}
			
			var members = type['class'].members;
			var attributeIndex = 0;
			for(var i=0, l=members.length; i<l; i++) {
				var member = members[i];
				if(member.name === attributeName) {
					return attributeIndex;
				}
				if(member.membertype === 'attribute') {
					++attributeIndex;
				}
			}
			return null;
		};
		
		// On remplit les attributes index des types
		data.ui.windows.forEach(function(window) {
			window.commands.forEach(function(command) {
				var display = command.display;
				if(display.component && display.attribute) {
					display.attributeIndex = getAttributeIndex(display.component, display.attribute);
				}
			});
			
			window.texts.forEach(function(text) {
				if(text.context) {
					text.context.forEach(function(item) {
						item.attributeIndex = getAttributeIndex(item.component, item.attribute);
					});
				}
			});
		});
		
		callback(null, data.ui);
	});
};

module.exports.get = get;
