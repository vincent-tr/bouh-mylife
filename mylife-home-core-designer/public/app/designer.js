/**
 * Gestion du design
 */

'use strict';

var module = angular.module('mylife.designer', ['mylife.api', 'mylife.tools', 'mylife.plumbHelper', 'mylife.schemaHelper']);

module.controller('designerController', ['$scope', '$timeout', 'dataAccess', 'plumbHelper', 'schemaHelper', 'dialogPrompt', function($scope, $timeout, dataAccess, plumbHelper, schemaHelper, dialogPrompt) {

	$scope.pluginTypes = [];
	$scope.plugins = [];
	$scope.hardware = [];
	$scope.links = [];
	$scope.selectedComponent = null;
	
	$scope.schemaHelper = schemaHelper;

	var applyData = function(data) {
		$scope.pluginTypes = data.pluginTypes;
		$scope.plugins = data.plugins;
		$scope.hardware = data.hardware;
		$scope.links = data.links;
		$scope.selectedComponent = null;
	};
	
	$scope.reload = function() {
		dataAccess.load(applyData);
	};
	
	$scope.save = function() {
		dataAccess.save($scope, function() {
			// TODO
		});
	};
	
	$scope.addHardware = function() {
		dialogPrompt({text: 'Saissez l\'url du matériel', defaultValue: 'http://host:8888', callbackOk: function(url) {
			dataAccess.loadHardware($scope, url);
		}});
	};
	
	$scope.createPlugin = function(typeId, x, y) {
		schemaHelper.createPlugin($scope, typeId, x, y);
	};
	
	$scope.selectedComponentDelete = function() {
		schemaHelper.deleteComponent($scope, $scope.selectedComponent);
		$scope.selectedComponent = null;
	};
	
	$scope.init = function() {
		plumbHelper.initBindings($scope);
		$scope.reload();
	};
}]);

module.directive('initializer', [ '$timeout', function($timeout) {
	return {
		restrict : 'A', 
		terminal : true,
		transclude : true,
		link : function(scope, element, attrs) {
			$timeout(scope.init, 0);
		}
	};
}]);

module.directive('toolboxItem', function() {
	return {
		replace: true,
		controller: 'designerController',
		link: function (scope, element, attrs) {
			
			$(element).draggable({
				revert: true,
				helper: 'clone',
				containment: $('#main'),
				zIndex: 100
			});
		}
	};
});

module.directive('schemaItem', ['plumbHelper', function(plumbHelper) {
	return {
		replace: true,
		controller: 'designerController',
		link: function (scope, element, attrs) {

			jsPlumb.draggable(element, {
				containment: 'parent'
			});
			
			element.bind('click', function() {
				$(element).addClass('item-selected').siblings().removeClass('item-selected');
				
				// Soit un plugin soit un hwitem (dans les ng-repeat)
				var component = scope.plugin;
				if(!component)
					component = scope.hwitem;
				
				// Scope parent du ng-repeat
				scope.$parent.selectedComponent = component;
			});
		}
	};
}]);

module.directive('componentAttribute', ['plumbHelper', function(plumbHelper) {
	return {
		replace: true,
		controller: 'designerController',
		link: function (scope, element, attrs) {
			plumbHelper.makeSource(element);
		}
	};
}]);


module.directive('componentAction', ['plumbHelper', function(plumbHelper) {
	return {
		replace: true,
		controller: 'designerController',
		link: function (scope, element, attrs) {
			plumbHelper.makeTarget(element);
		}
	};
}]);

module.directive('schemaLink', ['plumbHelper', function(plumbHelper) {
	return {
		replace: true,
		controller: 'designerController',
		link: function(scope, element, attrs){
			plumbHelper.createConnection(scope.link);
			
			scope.$on('$destroy', function() {
				plumbHelper.destroyConnection(scope.link);
			});
		}
	};
}]);

module.directive('schemaContainer', function($compile) {
	return {
		restrict: 'A',
		link: function(scope, element, attrs){

			element.droppable({
				drop: function(event, ui) {
					scope.$apply(function() {
						
						var typeId = angular.element(ui.draggable).data('identifier'),
						dragElement = angular.element(ui.draggable),
						dropElement = element;
	
						// if dragged item has class menu-item and dropped div has class drop-container, add module 
						if (dragElement.hasClass('toolbox-item') && dropElement.hasClass('drop-container')) {
							var x = event.pageX - dropElement.offset().left;
							var y = event.pageY - dropElement.offset().top;
	
							scope.createPlugin(typeId, x, y);
						}
					});
				}
			});
		}
	};
});
