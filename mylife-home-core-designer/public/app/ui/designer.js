/**
 * Gestion du design
 */

'use strict';

var module = angular.module('mylife.ui.designer', ['mylife.ui.dataAccess', 'mylife.tools', 'mylife.ui.fileReader', 'mylife.idGenerator']);

module.controller('uiController', ['$scope', '$modal', '$timeout', 'uiDataAccess', 'dialogAlert', 'idGenerator', 'tools', function($scope, $modal, $timeout, uiDataAccess, dialogAlert, idGenerator, tools) {
	
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
		
		var item = $scope.ui.selectedItem;
		if(!item) {
			return;
		}
		
		switch(item.internal().type) {
		case 'application':
			return;
			
		case 'window':
			tools.removeFromArray($scope.windows, item);
			break;
			
		case 'command':
			tools.removeFromArray(item.internal().parent.commands, item);
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
	
	$scope.formatCommandAction = function(action, short) {
		switch(action.type) {
		case 'component':
			if(short) {
				return action.component;
			}
			
			return 'component : ' + action.component + ':' + action.componentAction;
			
		case 'window':
			if(short) {
				return action.window;
			}
			
			var value = 'window : ' + action.window;
			if(action.popup) {
				value += ' (popup)';
			}
			return value;
		}
		
		return null;
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
		
		var data = {
			type: action.type,
			component: action.component,
			componentAction: action.componentAction,
			window: action.window,
			popup: action.popup
		};
		
		var componentActions = [];
		$scope.components.plugins.forEach(function(component) {
			component.internal().type['class'].members.forEach(function(member) {
				if(member.membertype === 'action' && member.arguments.length === 0) {
					componentActions.push({
						component: component.id,
						action: member.name
					});
				}
			});
		});
		
		var windows = $scope.windows;
		
		var modalInstance = $modal.open({
			templateUrl: 'designCommandAction.html',
			controller: function ($scope, $modalInstance) {

				$scope.data = data;
				$scope.componentActions = componentActions;
				$scope.windows = windows;
				
				$scope.changeComponentAction = function(componentAction) {
					data.component = componentAction.component;
					data.componentAction = componentAction.action;
				};
				
				$scope.ok = function () {
					$modalInstance.close();
				};

				$scope.cancel = function () {
					$modalInstance.dismiss();
				};
			}
		});

		modalInstance.result.then(function () {
			action.type = data.type;
			action.component = data.component;
			action.componentAction = data.componentAction;
			action.window = data.window;
			action.popup = data.popup;
		});
	};
	
	$scope.designCommandDisplay = function(display) {

		var componentAttributes = [];
		$scope.components.plugins.forEach(function(component) {
			component.internal().type['class'].members.forEach(function(member) {
				if(member.membertype === 'attribute') {
					componentAttributes.push({
						component: component.id,
						attribute: member.name,
						type: member.type
					});
				}
			});
		});
		
		var data = {
			component: display.component,
			attribute: display.attribute,
			defaultImage: display.defaultImage,
			map: []
		};
		if(display.map) {
			data.map = tools.clone(display.map);
		}
		
		// On recherche le type si un attribut est déjà sélectionné
		componentAttributes.forEach(function(componentAttribute) {
			if(componentAttribute.component !== data.component)
				return;
			if(componentAttribute.attribute !== data.attribute)
				return;
			data.type = componentAttribute.type;
		});
		
		var resources = $scope.resources;
		
		var modalInstance = $modal.open({
			templateUrl: 'designCommandDisplay.html',
			controller: function ($scope, $modalInstance) {

				$scope.data = data;
				$scope.componentAttributes = componentAttributes;
				$scope.resources = resources;
				
				$scope.changeComponentAttribute = function(componentAttribute) {
					data.component = componentAttribute.component;
					data.attribute = componentAttribute.attribute;
					data.type = componentAttribute.type;
				};
				
				$scope.deleteItem = function(item) {
					tools.removeFromArray(data.map, item);
				};
				
				$scope.ok = function () {
					$modalInstance.close();
				};

				$scope.cancel = function () {
					$modalInstance.dismiss();
				};
			}
		});

		modalInstance.result.then(function () {
			display.component = data.component;
			display.attribute = data.attribute;
			display.defaultImage = data.defaultImage;
			display.map = data.map;
		});
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