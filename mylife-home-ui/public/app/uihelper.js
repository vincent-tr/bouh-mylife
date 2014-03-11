/**
 * Helper pour ui
 */

'use strict';

var module = angular.module('mylife.uihelper', ['mylife.tools', 'mylife.net']);

/**
 * Gestion du mouseup/mousedown
 */
module.factory('mouseManager', ['$log', function($log) {
	
	var mouseDownEvent = null;
	
	return {
		
		commandMouseDown : function(command) {
			var timestamp = new Date().getTime();
			//$log.debug('mouse down on command ' + command.id + ' at ' + timestamp);
			mouseDownEvent = {
				command : command,
				timestamp : timestamp
			};
		},
		
		commandMouseUp : function(command) {
			var timestamp = new Date().getTime();
			//$log.debug('mouse up on command ' + command.id + ' at ' + timestamp);
			if(!mouseDownEvent) {
				return;
			}
			
			if(mouseDownEvent.command.id === command.id) {
				var elapsed = timestamp - mouseDownEvent.timestamp;
				//$log.debug('elapsed : ' + elapsed);
				if(elapsed < 2000) {
					command.primaryAction();
				} else {
					command.secondaryAction();
				}
			}
			mouseDownEvent = null;
		}
	};
}]);

module.factory('uihelper', ['$log', 'tools', 'net', 'mouseManager', function($log, tools, net, mouseManager) {

	var findResource = function(structure, id) {
		var resource = tools.arrayFind(structure.resources, function(res) { return res.id === id; });
		if(!resource) {
			return null;
		}
		return resource.data;
	};

	var createCommand = function(structure, swindow, scommand) {
		var imageGetter = function(sdisplay) {
			return function() {
				// TODO
				return findResource(structure, sdisplay.defaultImage);
			};
		};
		
		var actionGetter = function(saction, type) {
			return function() {
				$log.debug('execute action : ' + swindow.id + ':' + scommand.id + ' (' + type + ')');
				// TODO
			};
		};
		
		var attachBehavior = function(command) {

			command.mouseDown = function() {
				mouseManager.commandMouseDown(command);
			};
			command.mouseUp = function() {
				mouseManager.commandMouseUp(command);
			};
		};
		
		var command = {
			structure: scommand,
			id: swindow.id + ':' + scommand.id,
			x: scommand.x,
			y: scommand.y,
			image: imageGetter(scommand.display),
			primaryAction: actionGetter(scommand.primaryAction, 'primary'),
			secondaryAction: actionGetter(scommand.secondaryAction, 'secondary'),
		};
		
		attachBehavior(command);
		return command;
	};
	
	return {
		findResource: findResource,
		createCommand: createCommand
	};
}]);
