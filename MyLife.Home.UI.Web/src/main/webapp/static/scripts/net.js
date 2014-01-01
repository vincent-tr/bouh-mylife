/**
 * Gestion du service d'accès à la communication
 */

// https://github.com/gdi2290/angular-websocket

				
/*
 * Format de message :
 * 
 * c -> s : window window1.id window2.id
 * 
 * c -> s : action window.id component.id primary/secondary (seulement
 * si core action)
 * 
 * s -> c : icon window.id component.id image.id
 * 
 * s -> c : online window.id component.id
 * 
 * s -> c : offline window.id component.id
 * 
 * s -> c : structureChanged
 */

'use strict';

var net = angular.module('mylife.net', ['angular-websocket', 'mylife.urlHelper']);

net.config(['$provide', 'WebSocketProvider', 'urlHelperProvider', function($provide, WebSocketProvider, urlHelperProvider) {
	
	WebSocketProvider.prefix('').uri(urlHelperProvider.webSocket());

	$provide.factory('net', ['$log', 'WebSocket', function($log, WebSocket) {
		
		WebSocket.onopen(function() {
			$log.debug('connection opened');
			sendWindows();
		});
		
		WebSocket.onclose(function(ev) {
			$log.debug('ws connection closed (code : ' + ev.code + ') : ' + ev.reason);
		});
		
		WebSocket.onerror(function() {
			$log.debug('ws connection error');
		});		
		
		WebSocket.onmessage(function(ev) {
			var msg = ev.data;
	    	$log.debug('ws receive : ' + msg);

			var args = msg.split(' ');
			if(args.length == 0)
				return;
			var command = args[0];
			args.shift();
			
			switch(command) {
			
			case 'icon':
				if(args.length < 3)
					return;
				receiveIcon(args[0], args[1], args[2]);
				break;
				
			case 'online':
				if(args.length < 2)
					return;
				receiveOnlineChanged(args[0], args[1], true);
				break;
				
			case 'offline':
				if(args.length < 2)
					return;
				receiveOnlineChanged(args[0], args[1], false);
				break;
				
			case 'structureChanged':
				receiveStructureChanged();
				break;
			}
		});
		
		var send = function(msg) {
			$log.debug('ws send : ' + msg);
			WebSocket.send(msg);
		};
		
		var windows = [];
		
		var windowPush = function(windowId) {
			windows.push(windowId);
			sendWindows();
		};
		
		var windowPop = function() {
			windows.pop();
			sendWindows();
		};
		
		var windowClear = function() {
			windows.length = 0;
			sendWindows();
		};
		
		var sendWindows = function() {
			var msg = 'window';
			for(var i=0, len=windows.length; i<len; i++)
				msg += ' ' + windows[i];
			send(msg);
		};
		
		var sendAction = function(windowId, componentId, isprimary) {
			var msg = 'action ' + windowId + ' ' + componentId + ' ' + (isprimary ? 'primary' : 'secondary');
			send(msg);
		};

		var receiveIcon = function(windowId, componentId, imageId) {
			// TODO
		};
		
		var receiveOnlineChanged = function(windowId, componentId, online) {
			if(!online) {
				// TODO : offline icon
			}
		};
		
		var receiveStructureChanged = function() {
			// TODO
		};
		
		return {
			windowPush : windowPush,
			windowPop : windowPop,
			windowClear : windowClear,
			sendAction : sendAction,
			// TODO : events
		};
	}]);
}]);
