/**
 * Gestion du design
 */

'use strict';

var module = angular.module('mylife.ui.designer', ['mylife.ui.dataAccess', 'mylife.tools', 'mylife.ui.fileReader']);

module.controller('uiController', ['$scope', '$timeout', 'uiDataAccess', function($scope, $timeout, uiDataAccess) {
	
	$scope.resources = [];
	$scope.windows = [];
	$scope.defaultWindow = '';
	
	$scope.init = function() {
	};
	
}]);