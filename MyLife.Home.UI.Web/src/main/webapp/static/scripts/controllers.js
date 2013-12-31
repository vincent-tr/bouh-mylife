/**
 * Gestion des contrôleurs
 */

'use strict';

var controllers = angular.module('mylife.controllers', []);

controllers.controller('windowController', 
		['$scope', '$log', 'window', 
		 function ($scope,$log, window) {
	$scope.window = window; 
	$log.debug('showing window : ' + window.id);
}]);