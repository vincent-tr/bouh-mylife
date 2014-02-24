/**
 * Gestion du design
 */

'use strict';

var module = angular.module('mylife.ui.designer', ['mylife.ui.dataAccess', 'mylife.tools', 'mylife.ui.fileReader']);

module.controller('uiController', ['$scope', '$timeout', 'uiDataAccess', function($scope, $timeout, uiDataAccess) {
	
	$scope.resources = [];
	$scope.windows = [];
	$scope.defaultWindow = '';
	$scope.components = [];
	
	var applyData = function(data) {
		$scope.resources = data.resources;
		$scope.windows = data.windows;
		$scope.defaultWindow = data.defaultWindow;
	};
	
	var applyComponents = function(components) {
		$scope.components = components;
	};
	
	var checkSchema = function() {
		
	};
	
	$scope.reload = function() {
		uiDataAccess.load(applyData);
		uiDataAccess.components(applyComponents);
	};

	$scope.save = function() {
		uiDataAccess.save($scope, function() {
			dialogAlert({text: 'Enregistrement effectué'});
		});
	};
	
	$scope.init = function() {
		$scope.reload();
	};

	$scope.newResource = function() {
		var res = {
			id: $scope.newResourceId, 
			data : $scope.newResourceData
		};
		$scope.resources.push(res);
	};
	
	$scope.destroyResource = function(resource) {
		var index = $scope.resources.indexOf(resource);
		if (index === -1) {
			return;
		}
		$scope.resources.splice(index, 1);
		checkSchema();
	};
	
}]);