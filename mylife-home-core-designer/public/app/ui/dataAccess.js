/**
 * Accès aux données
 */


'use strict';

var module = angular.module('mylife.ui.dataAccess', ['mylife.api', 'mylife.tools', 'ui.bootstrap']);

module.factory('uiDataAccess', ['$modal', 'api', 'tools', 'dialogAlert', function($modal, api, tools, dialogAlert) {
	
	var load = function(callback) {
		
		var prepareData = function(data) {
			if(!data.resources)
				data.resources = {};
			if(!data.windows)
				data.windows = {};
			if(!data.defaultWindow)
				data.defaultWindow = '';
			
			data.resources.forEach(tools.attachInternal);
			data.windows.forEach(tools.attachInternal);
			data.windows.forEach(function(window) {
				if(window.commands) {
					window.commands.forEach(tools.attachInternal);
				}
			});
		};
		
		api.ui.data.get({}, function(data) {
			prepareData(data);
			if(callback) {
				callback(data);
			}
		});
	};
	
	var save = function(data, callback) {
		
		var prepareArray = function(source) {
			return tools.clone(source);
		};

		var sendData = {
			resources: prepareArray(data.resources),
			windows: prepareArray(data.windows),
			defaultWindow: data.defaultWindow
		};
			
		api.ui.data.post({}, sendData, function() {
			dialogAlert({text: 'Enregistrement effectué'});
		});
	};
	
	var components = function(callback) {

		var prepareData = function(data) {

			var attachTypeToPlugin = function(plugin) {
				
				for (var i = 0, l = data.pluginTypes.length; i < l; i++) {
					var type = data.pluginTypes[i];
					if (type.id === plugin.type) {
						plugin.internal().type = type;
						break;
					}
				}
			};
		
			data.pluginTypes.forEach(tools.attachInternal);
			data.plugins.forEach(tools.attachInternal);
			
			data.plugins.forEach(attachTypeToPlugin);
		};
		
		api.ui.components.get({}, function(data) {
			prepareData(data);
			if(callback) {
				callback(data);
			}
		});
	};
	
	return {
		load: load,
		save: save,
		components: components
	};
	
}]);
