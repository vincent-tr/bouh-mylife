/**
 * Controleurs
 */

'use strict';

var controllers = angular.module('mylife.controllers', []);

controllers.controller('windowController', ['$scope', '$log', 'structure', 'windowId', 'popup', function ($scope, $log, structure, windowId, popup) {
	$log.debug('showing window : ' + windowId + ' (popup : ' + popup + ')');
	
	//$scope.popup = popup;
}]);
