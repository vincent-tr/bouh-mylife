/**
 * Helper pour ui
 */

'use strict';

var module = angular.module('mylife.uihelper', ['mylife.tools', 'mylife.net']);

module.factory('uihelper', ['$log', '$location', '$modal', 'tools', 'net', function($log, $location, $modal, tools, net) {

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
		
		var command = {
			structure: scommand,
			id: swindow.id + ':' + scommand.id,
			x: scommand.x,
			y: scommand.y,
			image: imageGetter(scommand.display),
			primaryAction: actionGetter(scommand.primaryAction, 'primary'),
			secondaryAction: actionGetter(scommand.secondaryAction, 'secondary'),
		};
		return command;
	};
	
	return {
		findResource: findResource,
		createCommand: createCommand
	};
}]);

module.directive('inputHandler', ['$parse', 'inputManager', function($parse, inputManager) {
	return {
		restrict: 'A',
		link: function(scope, element, attrs){

			function isTouchDevice() {
				return !!('ontouchstart' in window);
			}
			
			var config = $parse(attrs.inputHandler)(scope);
			var manager = inputManager(config);
			
			if(isTouchDevice()) {
				element.bind('touchstart', function(event) {
					manager.down();
					event.preventDefault();
				});
				element.bind('touchend', function(event) {
					manager.up();
					event.preventDefault();
				});
			} else {
				element.bind('mousedown', function(event) {
					manager.down();
					event.preventDefault();
				});
				element.bind('mouseup', function(event) {
					manager.up();
					event.preventDefault();
				});
			}
		}
	};
}]);

/**
 * Gestion du mouseup/mousedown
 */
module.factory('inputManager', ['$log', '$timeout', function($log, $timeout) {

	return function(config) {
		
		var lastDown = null;
		var eventStack = '';
		var endWait = null;
		
		var executeEvents = function() {
			$log.debug('inputManager: execute events : \'' + eventStack + '\')');
			
			var fn = config[eventStack];
			if(fn) {
				fn();
			}
		};
		
		return {
			
			down: function() {
				// Pas de fin de saisie de suite
				if(endWait) {
					$timeout.cancel(endWait);
				}
				
				lastDown = {
					timestamp: new Date().getTime()
				};
			},
			
			up: function() {
				// Pas de fin de saisie de suite
				if(endWait) {
					$timeout.cancel(endWait);
				}
				// Si pas de down, tchao
				if(!lastDown) {
					eventStack = '';
					return;
				}
				
				// Prise en compte de l'event 
				var down_ts = lastDown.timestamp;
				var up_ts = new Date().getTime();
				lastDown = null;
				
				// Ajout de l'event
				if(up_ts - down_ts < 500) {
					eventStack += 's';
				} else {
					eventStack += 'l';
				}
				
				// Attente de la fin de saisie
				endWait = $timeout(function() {
					executeEvents();
					
					eventStack = '';
					endWait = null;
				}, 300);
			}
		};
	};
}]);
