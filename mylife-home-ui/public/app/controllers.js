/**
 * Controleurs
 */

'use strict';

var module = angular.module('mylife.controllers', ['mylife.tools', 'mylife.net', 'mylife.uihelper']);

module.controller('windowController',
		['$scope', '$log', '$location', 'tools', 'net', 'uihelper', 'structure', 'windowId', 'popup',
		 function ($scope, $log, $location, tools, net, uihelper, structure, windowId, popup) {
			
	$log.debug('showing window : ' + windowId + ' (popup : ' + popup + ')');
	
	$scope.connected = function() { return net.connected; };
	//$scope.structure = structure;
	//$scope.windowId = windowId;
	$scope.popup = popup;
	
	var swindow = tools.arrayFind(structure.windows, function(window) { return window.id === windowId; });
	
	$scope.window = {
		structure: swindow,
		image: uihelper.findResource(structure, swindow.background),
		commands: tools.arraySelect(swindow.commands, function(scommand) { return uihelper.createCommand(structure, swindow, scommand); })
	};
}]);
