/**
 * Gestion de la création du modèle
 */

'use strict';

angular.module('mylife.modelBuilder', ['mylife.structure', 'mylife.images'], function($provide) {
	$provide.factory('modelBuilder', ['$log', '$q', 'structure', 'images', function($log, $q, structure, images) {

		/**
		 * Gestion du mouseup/mousedown
		 */
		var mouseManager = {
		
			mouseDownEvent : null,
			
			componentMouseDown : function(component) {
				var timestamp = new Date().getTime();
				//$log.debug('mouse down on component ' + component.id + ' at ' + timestamp);
				this.mouseDownEvent = {
					component : component,
					timestamp : timestamp
				};
			},
			
			componentMouseUp : function(component) {
				var timestamp = new Date().getTime();
				//$log.debug('mouse up on component ' + component.id + ' at ' + timestamp);
				if(this.mouseDownEvent == null)
					return;
				if(this.mouseDownEvent.component.id == component.id) {
					var elapsed = timestamp - this.mouseDownEvent.timestamp;
					//$log.debug('elapsed : ' + elapsed);
					if(elapsed < 2000)
						component.primaryAction();
					else
						component.secondaryAction();
				}
				this.mouseDownEvent = null;
			}
		};

		/**
		 * Obtention d'images en batch
		 */
		var imagesBatch = function(keyMap) {
			var promises = {};
			for(var keyName in keyMap) {
				var keyValue = keyMap[keyName];
				promises[keyName] = images(keyValue);
			}
			return $q.all(promises);
		};
		
		/**
		 * Obtention des clés d'images à obtenir depuis le composant spécifié
		 */
		var componentImageKeys = function(component, keys) {

			var icon = component.icon;
			if(icon == null || icon == undefined)
				return;

			// Si image statique
			var staticIconId = icon.iconId;
			if(staticIconId != null && staticIconId != undefined) {
				keys['component/' + component.id + '/staticIconId'] = staticIconId;
				return;
			}
			
			// Si image dynamique
			var defaultIconId = icon.defaultIconId;
			if(defaultIconId == null || defaultIconId == undefined)
				return;
			
			keys['component/' + component.id + '/defaultIconId'] = defaultIconId;
			var mappings = icon.mappings;
			for(var i=0, len=mappings.length; i<len; i++) {
				var mapping = mappings[i];
				keys['component/' + component.id + '/mappings/' + i + '/iconId'] = mapping.iconId;
			}
			return keys;
		};
		
		/**
		 * Obtention des clés d'images à obtenir depuis la fenêtre spécifiée
		 */
		var imageKeys = function(window, keys) {
			if(window.backgroundId != null && window.backgroundId != undefined)
				keys['window/backgroundId'] = window.backgroundId;
			
			var components = window.components;
			for(var i=0, len=components.length; i<len; i++) {
				var component = components[i];
				componentImageKeys(component, keys);
			}
			
			return keys;
		};
		
		/**
		 * Construction du composant du modèle avec le tableau d'images
		 */
		var componentBuilder = function(componentSource, imageMap) {
			
			var component = {
				__component : componentSource,
				id : componentSource.id,
				displayName : componentSource.displayName,
				staticIconId : imageMap['component/' + componentSource.id + '/staticIconId'],
				defaultIconId : imageMap['component/' + componentSource.id + '/defaultIconId'],
			};
			
			component.primaryAction = angular.bind(component, function() {
				$log.debug('primary action on component' + this.id);
				// TODO
			});
			
			component.secondaryAction = angular.bind(component, function() {
				$log.debug('secondary action on component' + this.id);
				// TODO
			});
			
			component.mouseDown = angular.bind(mouseManager, mouseManager.componentMouseDown, component);
			component.mouseUp = angular.bind(mouseManager, mouseManager.componentMouseUp, component);
			
			// TODO
			component.image = component.defaultIconId;
			
			return component;
		};
		
		/**
		 * Construction du modèle avec le tableau d'images
		 */
		var builder = function(windowSource, imageMap) {
			var window = {
					__window : windowSource,
					id : windowSource.id,
					displayName : windowSource.displayName,
					backgroundImage : imageMap['window/backgroundId'],
					components : []
				};
			
			var componentsSource = windowSource.components;
			for(var i=0, len=componentsSource.length; i<len; i++) {
				var componentSource = componentsSource[i];
				var component = componentBuilder(componentSource, imageMap);
				window.components.push(component);
			}
				
			return window;
		};
		
		/**
		 * Fabrique
		 */
		return function($scope, window) {
			
			var keys = {};
			imageKeys(window, keys);
			imagesBatch(keys).then(function(imageMap) {
				$scope.window = builder(window, imageMap);
			});
		};
	}]);
});
