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
			mouseDownEvent = {
				command : command,
				timestamp : timestamp
			};
		},
		
		commandMouseUp : function(command) {
			var timestamp = new Date().getTime();
			if(!mouseDownEvent) {
				return;
			}
			
			if(mouseDownEvent.command.id === command.id) {
				var elapsed = timestamp - mouseDownEvent.timestamp;
				if(elapsed < 2000) {
					//command.primaryAction();
				} else {
					//command.secondaryAction();
				}
			}
			mouseDownEvent = null;
		},
		
		commandSglclick : function(command) {
			command.primaryAction();
		},
		
		commandDblclick : function(command) {
			command.secondaryAction();
		}
	};
}]);

module.factory('uihelper', ['$log', '$location', '$modal', 'tools', 'net', 'mouseManager', function($log, $location, $modal, tools, net, mouseManager) {

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
				if(sdisplay.component && sdisplay.attribute && sdisplay.map && sdisplay.map.length > 0) {
					var value = net.componentAttribute(sdisplay.component, sdisplay.attributeIndex);
					if(value) {
						// Recherche de mapping
						for(var i=0, l=sdisplay.map.length; i<l; i++) {
							var item = sdisplay.map[i];
							// Attention : si numérique on doit caster ici !
							if(item.value == value) {
								return findResource(structure, item.image);
							}
						}
					}
				}
				// Valeur par défaut
				return findResource(structure, sdisplay.defaultImage);
			};
		};
		
		var popup = function(windowId) {
			
			var modalInstance = $modal.open({
				controller : 'windowController',
				templateUrl : 'popup.html',
				resolve : {
					'structure': function() { return structure; },
					'windowId': function() { return windowId; },
					'popup': function() { return true; }
				}
			});
			
			modalInstance.result.then(function() {
				// rien à faire
			});
		};
		
		var actionGetter = function(saction, type) {
			return function() {
				$log.debug('execute action : ' + swindow.id + ':' + scommand.id + ' (' + type + ')');
				
				switch(saction.type) {
				case 'window':
					if(saction.popup) {
						popup(saction.window);
					} else {
						$location.path('/' + saction.window);
					}
					break;
				case 'component':
					net.action(saction.component, saction.componentAction);
					break;
				}
			};
		};
		
		var attachBehavior = function(command) {

			command.mouseDown = function() {
				mouseManager.commandMouseDown(command);
			};
			command.mouseUp = function() {
				mouseManager.commandMouseUp(command);
			};
			command.sglclick = function() {
				mouseManager.commandSglclick(command);
			};
			command.dblclick = function() {
				mouseManager.commandDblclick(command);
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
