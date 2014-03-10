/**
 * Controleurs
 */

'use strict';

var controllers = angular.module('mylife.controllers', []);

controllers.controller('windowController', 
		['$scope', '$log',
		 function ($scope, $log) {
	$log.debug('showing window : ' + window.id + ' (popup : ' + popup + ')');
	
	//$scope.popup = popup;
}]);
