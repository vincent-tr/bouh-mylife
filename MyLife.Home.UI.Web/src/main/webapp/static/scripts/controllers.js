/**
 * Gestion des contrôleurs
 */

'use strict';

var controllers = angular.module('mylife.controllers', ['mylife.modelBuilder', 'mylife.net']);

controllers.controller('windowController', 
		['$scope', '$log', 'window', 'modelBuilder', 'net',
		 function ($scope,$log, window, modelBuilder, net) {
	$log.debug('showing window : ' + window.id);
	
	net.windowClear();
	net.windowPush(window.id);
	modelBuilder($scope, window);
}]);