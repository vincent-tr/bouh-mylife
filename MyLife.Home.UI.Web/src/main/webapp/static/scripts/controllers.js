/**
 * Gestion des contrôleurs
 */

'use strict';

var controllers = angular.module('mylife.controllers', ['mylife.images']);

controllers.controller('windowController', 
		['$scope', '$log', 'window', 'images',
		 function ($scope,$log, window, images) {
	$log.debug('showing window : ' + window.id);
	
	var getImage = function(imageId) {
		if(imageId == undefined)
			return null;
		if(imageId == null)
			return null;
		
		$log.debug('getting image ' + imageId);
		return images(imageId);
	};
	
	var componentImageSelector = function(component) {
		var icon = component.icon;
		if(icon == null || icon == undefined)
			return null;
		
		// Si image statique
		var staticIconId = icon.iconId;
		if(staticIconId != null && staticIconId != undefined)
			return getImage(staticIconId);
		
		// Si image dynamique
		var defaultIconId = icon.defaultIconId;
		if(defaultIconId == null || defaultIconId == undefined)
			return null;
		var mappings = icon.mappings;
		// TODO
		return null; // getImage(defaultIconId);
	};

	var mouseDownEvent = null;
	
	var componentMouseDown = function(component) {
		var timestamp = new Date().getTime();
		//$log.debug('mouse down on component ' + component.id + ' at ' + timestamp);
		mouseDownEvent = {
			component : component,
			timestamp : timestamp
		};
	};
	
	var componentMouseUp = function(component) {
		var timestamp = new Date().getTime();
		//$log.debug('mouse up on component ' + component.id + ' at ' + timestamp);
		if(mouseDownEvent == null)
			return;
		if(mouseDownEvent.component.id == component.id) {
			var elapsed = timestamp - mouseDownEvent.timestamp;
			//$log.debug('elapsed : ' + elapsed);
			if(elapsed < 2000)
				primaryAction(component);
			else
				secondaryAction(component);
		}
		mouseDownEvent = null;
	};
	
	var primaryAction = function(component) {
		$log.debug('primary action on component' + component.id);
		var action = component.primaryAction;
		executeAction(action);
	};
	
	var secondaryAction = function(component) {
		$log.debug('secondary action on component' + component.id);
		var action = component.secondaryAction;
		executeAction(action);
	};
	
	var executeAction = function(action) {
		// TODO
	};
	
	$scope.window = window;
	$scope.componentImageSelector = componentImageSelector;
	$scope.getImage = getImage;
	$scope.componentMouseDown = componentMouseDown;
	$scope.componentMouseUp = componentMouseUp;
}]);