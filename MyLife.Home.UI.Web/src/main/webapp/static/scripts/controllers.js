/**
 * Gestion des contrôleurs
 */

'use strict';

var controllers = angular.module('mylife.controllers', ['mylife.modelBuilder']);

controllers.controller('windowController', 
		['$scope', '$log', 'window', 'modelBuilder',
		 function ($scope,$log, window, modelBuilder) {
	$log.debug('showing window : ' + window.id);
	modelBuilder($scope, window);
}]);