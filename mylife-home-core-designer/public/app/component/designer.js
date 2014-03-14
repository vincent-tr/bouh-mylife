/**
 * Gestion du design
 */

'use strict';

var module = angular.module('mylife.component.designer', ['mylife.component.dataAccess', 'mylife.tools', 'mylife.component.plumbHelper', 'mylife.component.schemaHelper']);

module.controller('componentController', ['$scope', '$timeout', 'componentDataAccess', 'plumbHelper', 'schemaHelper', 'dialogPrompt', 'dialogAlert', function($scope, $timeout, componentDataAccess, plumbHelper, schemaHelper, dialogPrompt, dialogAlert) {

	$scope.pluginTypes = [];
	$scope.plugins = [];
	$scope.hardware = [];
	$scope.links = [];

	$scope.ui = {
		selectedComponent: null,
		schemaZoom: 1.0
	};
	
	$scope.ui.schemaZoomPercent = function() {
		return Math.round($scope.ui.schemaZoom * 100);
	};
	
	$scope.schemaHelper = schemaHelper;

	var applyData = function(data) {
		$scope.pluginTypes = data.pluginTypes;
		$scope.plugins = data.plugins;
		$scope.hardware = data.hardware;
		$scope.links = data.links;
		$scope.ui.selectedComponent = null;
	};
	
	$scope.reload = function() {
		componentDataAccess.load(applyData);
	};
	
	$scope.save = function() {
		componentDataAccess.save($scope, function() {
			dialogAlert({text: 'Enregistrement effectué'});
		});
	};
	
	$scope.addHardware = function() {
		dialogPrompt({text: 'Saissez l\'url du matériel', defaultValue: 'http://host:8888', callbackOk: function(url) {
			componentDataAccess.loadHardware($scope, url);
		}});
	};
	
	$scope.createPlugin = function(typeId, x, y) {
		schemaHelper.createPlugin($scope, typeId, x, y);
	};
	
	$scope.selectedComponentDelete = function() {
		schemaHelper.deleteComponent($scope, $scope.ui.selectedComponent);
		$scope.ui.selectedComponent = null;
	};
	
	$scope.init = function() {
		plumbHelper.initBindings($scope);
		$scope.reload();
	};
	
	$scope.zoomOut = function() {
		var zoom = $scope.ui.schemaZoom;
		if(zoom <= 0.15) // arrondis ...
			return;
		zoom -= 0.1;
		zoom = Math.round(zoom*100)/100;
		$scope.ui.schemaZoom = zoom;
	};
	
	$scope.zoomIn = function() {
		var zoom = $scope.ui.schemaZoom; 
		if(zoom >= 1.95) // arrondis ...
			return;
		zoom += 0.1;
		zoom = Math.round(zoom*100)/100;
		$scope.ui.schemaZoom = zoom;
	};
	
	$scope.applyZoom = function(val) {
		return val * $scope.ui.schemaZoom;
	};
}]);


module.directive('toolboxItem', function() {
	return {
		replace: true,
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
		link: function (scope, element, attrs) {

			// Soit un plugin soit un hwitem (dans les ng-repeat)
			var component = scope.plugin;
			if(!component)
				component = scope.hwitem;
			
			jsPlumb.draggable(element, {
				containment: 'parent',
				drag: function( event, ui ) {
					scope.$apply(function() {
						component.designer.x = ui.position.left;
						component.designer.y = ui.position.top;
					});
				}
			});
			
			element.bind('click', function() {
				$(element).addClass('item-selected').siblings().removeClass('item-selected');
				
				// Scope parent du ng-repeat
				scope.ui.selectedComponent = component;
			});
		}
	};
}]);

module.directive('componentAttribute', ['plumbHelper', function(plumbHelper) {
	return {
		replace: true,
		controller: 'componentController',
		link: function (scope, element, attrs) {
			plumbHelper.makeSource(element);
		}
	};
}]);


module.directive('componentAction', ['plumbHelper', function(plumbHelper) {
	return {
		replace: true,
		controller: 'componentController',
		link: function (scope, element, attrs) {
			plumbHelper.makeTarget(element);
		}
	};
}]);

module.directive('schemaLink', ['plumbHelper', function(plumbHelper) {
	return {
		replace: true,
		controller: 'componentController',
		link: function(scope, element, attrs){
			plumbHelper.createConnection(scope.link);
			
			scope.$on('$destroy', function() {
				plumbHelper.destroyConnection(scope.link);
			});
		}
	};
}]);

module.directive('schemaContainer', ['$compile', '$timeout', function($compile, $timeout) {
	return {
		restrict: 'A',
		link: function(scope, element, attrs){
			
			var setZoom = function(newValue, oldValue) {
				
			    var prefixes = [ "-webkit-", "-moz-", "-ms-", "-o-", "" ],
			        scale = "scale(" + newValue + ")";
				
			    var wrapperElement = element.parent();
				var scrollHeight = wrapperElement.scrollTop() / ((element.height() * oldValue) - wrapperElement.height());
				var scrollWidth = wrapperElement.scrollLeft() / ((element.width() * oldValue) - wrapperElement.width());

			    for (var i = 0; i < prefixes.length; i++) {
			    	element.css(prefixes[i] + "transform", scale);
			    	element.css(prefixes[i] + "transform-origin", '0 0 0');
			    }

			    jsPlumb.setZoom(newValue);
			    
			    $timeout(function() {
					var scrollTop = ((element.height() * newValue) - wrapperElement.height()) * scrollHeight;
					var scrollLeft = ((element.width() * newValue) - wrapperElement.width()) * scrollWidth;
					//var scrollTop = wrapperElement.scrollTop();
					//var scrollLeft = wrapperElement.scrollLeft();
					
			    	wrapperElement.scrollTop(0);
			    	wrapperElement.scrollLeft(0);
			    	jsPlumb.repaintEverything();

			    	wrapperElement.scrollTop(scrollTop);
			    	wrapperElement.scrollLeft(scrollLeft);
			    });
			};
			
			scope.$watch('ui.schemaZoom', function(newValue, oldValue) {
				setZoom(newValue);
			});

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
}]);
