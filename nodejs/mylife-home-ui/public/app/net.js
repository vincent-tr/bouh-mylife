/**
 * Comunication
 */

'use strict';

var module = angular.module('mylife.net', []);

module.factory('socket', function($rootScope) {
	var socket = io.connect();
	return {
		on : function(eventName, callback) {
			socket.on(eventName, function() {
				var args = arguments;
				$rootScope.$apply(function() {
					callback.apply(socket, args);
				});
			});
		},
		emit : function(eventName, data, callback) {
			socket.emit(eventName, data, function() {
				var args = arguments;
				$rootScope.$apply(function() {
					if (callback) {
						callback.apply(socket, args);
					}
				});
			});
		}
	};
});

module.factory('net', ['socket', function(socket) {
	
	var service = {
		net : {},
		connected: false
	};
	
	var net = service.net;
	
	var partall = function() {
		for(var id in net) {
			if(net.hasOwnProperty(id)) {
				part(id);
			}
		}
	};
	
	var join = function(id, attrs) {
		net[id] = {
			id: id,
			attrs: attrs
		};
		service.connected = true;
	};
	
	var part = function(id) {
		delete net[id];
	};
	
	var changed = function(id, attrs) {
		net[id].attrs = attrs;
	};

	socket.on('connect', function() {
		//service.connected = true;
	});
	
	socket.on('disconnect', function() {
		partall();
		service.connected = false;
	});
	
	socket.on('partall', function(data) {
		partall();
		service.connected = false;
	});

	socket.on('part', function(data) {
		var parts = data.nick.split('|');
		part(parts[0]);
	});

	socket.on('join', function(data) {
		var parts = data.nick.split('|');
		join(parts[0], parts.slice(1));
	});

	socket.on('changed', function(data) {
		var parts = data.newnick.split('|');
		changed(parts[0], parts.slice(1));
	});
	
	service.action = function(componentId, actionName) {
		socket.emit('msg', {target: componentId, message: actionName});
	};
	
	service.componentAttribute = function(componentId, attributeIndex) {
		var component = service.net[componentId];
		if(!component) {
			return null;
		}
		if(component.attrs.length <= attributeIndex) {
			return null;
		}
		return component.attrs[attributeIndex];
	};
	
	return service;
	
}]);
