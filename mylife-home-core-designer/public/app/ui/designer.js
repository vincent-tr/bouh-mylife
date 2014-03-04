/**
 * Gestion du design
 */

'use strict';

var module = angular.module('mylife.ui.designer', ['mylife.ui.dataAccess', 'mylife.tools', 'mylife.ui.fileReader', 'mylife.idGenerator']);

module.controller('uiController', ['$scope', '$timeout', 'uiDataAccess', 'dialogAlert', 'idGenerator', 'tools', function($scope, $timeout, uiDataAccess, dialogAlert, idGenerator, tools) {
	
	var app = {
		defaultWindow: ''
	};
	tools.attachInternal(app);
	app.internal().type = 'application';
	
	$scope.resources = [];
	$scope.windows = [];
	$scope.components = [];
	$scope.app = app;
	
	$scope.ui = {
		selectedItem: null
	};
	
	var prepareCommand = function(command, parent) {
		command.internal().type = 'command';
		command.internal().parent = parent;
	};
	
	var prepareWindow = function(window) {
		window.internal().type = 'window';
		if(!window.commands)
			window.commands = [];
		window.commands.forEach(function(command) {
			prepareCommand(command, window);
		});
	};
	
	var applyData = function(data) {
		$scope.resources = data.resources;
		$scope.windows = data.windows;
		$scope.app.defaultWindow = data.defaultWindow;
		
		$scope.windows.forEach(prepareWindow);
	};
	
	var applyComponents = function(components) {
		$scope.components = components;
	};
	
	var checkSchema = function() {
		// TODO
	};
	
	$scope.reload = function() {
		uiDataAccess.load(applyData);
		uiDataAccess.components(applyComponents);
	};

	$scope.save = function() {
		var data = {
			resources: $scope.resources,
			windows: $scope.windows,
			defaultWindow: $scope.app.defaultWindow
		};
		
		uiDataAccess.save(data, function() {
			dialogAlert({text: 'Enregistrement effectué'});
		});
	};
	
	$scope.init = function() {
		$scope.reload();
	};

	$scope.newResource = function() {
		
		if(!$scope.newResourceId) {
			dialogAlert({text: 'Saisissez un id'});
			return;
		}
		
		if(!$scope.newResourceData) {
			dialogAlert({text: 'Saisissez un contenu'});
			return;
		}
		
		var res = {
			id: $scope.newResourceId, 
			data : $scope.newResourceData
		};
		$scope.resources.push(res);
		
		$scope.newResourceId = undefined;
		$scope.newResourceData = undefined;
	};
	
	$scope.destroyResource = function(resource) {
		var index = $scope.resources.indexOf(resource);
		if (index === -1) {
			return;
		}
		$scope.resources.splice(index, 1);
		
		checkSchema();
	};
	
	$scope.deleteItem = function() {
		
		var deleteWindow = function(window) {
			var index = $scope.windows.indexOf(window);
			if (index === -1) {
				return;
			}
			$scope.windows.splice(index, 1);
		};
		
		var deleteCommand = function(command) {
			var window = command.internal().parent;
			var index = window.commands.indexOf(command);
			if (index === -1) {
				return;
			}
			window.commands.splice(index, 1);
		};
		
		var item = $scope.ui.selectedItem;
		if(!item) {
			return;
		}
		
		switch(item.internal().type) {
		case 'application':
			return;
			
		case 'window':
			deleteWindow(item);
			break;
			
		case 'command':
			deleteCommand(item);
			break;
		}
		
		$scope.ui.selectedItem = null;
		checkSchema();
	};
	
	$scope.createItem = function() {
		
		var createWindow = function() {
			var window = {
				id: 'new_window_' + idGenerator()
			};
			
			tools.attachInternal(window);
			prepareWindow(window);
			
			$scope.windows.push(window);
		};
		
		var createCommand = function(window) {
			
			var command = {
				id: 'new_command_' + idGenerator(),
				display: {},
				primaryAction: {},
				secondaryAction: {}
			};
			
			tools.attachInternal(command);
			prepareCommand(command, window);
			window.commands.push(command);
		};

		var item = $scope.ui.selectedItem;
		if(!item) {
			return;
		}
		
		switch(item.internal().type) {
		case 'application':
			createWindow();
			break;
			
		case 'window':
			createCommand(item);
			break;
			
		case 'command':
			createCommand(item.internal().parent);
			break;
		}
	};
	
	$scope.formatCommandAction = function(action) {
		// TODO
	};
	
	$scope.formatCommandDisplay = function(display) {
		if(!display)
			return null;
		if(!display.defaultImage)
			return null;
		
		var value = display.defaultImage;
		if(display.map && display.map.length > 0) {
			value += ' (...)';
		}
		return value;
	};
	
	$scope.designCommandAction = function(action) {
		// TODO
	};
	
	$scope.designCommandDisplay = function(display) {
		// TODO
	};
}]);

module.directive('itemlistItem', [function() {
	return {
		replace: true,
		link: function (scope, element, attrs) {

			var item = scope.item;
			
			element.bind('click', function() {
				$('.ui-item-selected').removeClass('ui-item-selected');
				$(element).addClass('ui-item-selected');
				
				// Scope parent du ng-repeat
				scope.$apply(function() {
					scope.ui.selectedItem = item;
				});
			});
		}
	};
}]);