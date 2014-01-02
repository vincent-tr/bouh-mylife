/**
 * Gestion de la création du modèle
 */

'use strict';

angular.module('mylife.modelBuilder', ['mylife.structure', 'mylife.images', 'mylife.net', 'mylife.app'], function($provide) {
	$provide.factory('modelBuilder', ['$log', '$q', 'structure', 'images', 'net', 'showWindow', function($log, $q, structure, images, net, showWindow) {

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
					if(elapsed < 2000) {
						$log.debug('primary action on component' + component.id);
						component.primaryAction.execute();
					} else {
						$log.debug('secondary action on component' + component.id);
						component.secondaryAction.execute();
					}
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
			if(components != null && components != undefined) {
				for(var i=0, len=components.length; i<len; i++) {
					var component = components[i];
					componentImageKeys(component, keys);
				}
			}
			
			return keys;
		};
		
		var actionBuilder = function(actionSource, owner, isprimary) {

			var isdefined = actionSource != undefined && actionSource != null;
			var iswindow = isdefined && actionSource.windowId != null && actionSource.windowId != undefined;
			var iscore = isdefined && actionSource.componentId != null && actionSource.componentId != undefined;
			
			var action = {
				__action : actionSource,
				owner : owner,
				isprimary : isprimary,
				windowId : isdefined ? actionSource.windowId : undefined,
				popup : isdefined ? actionSource.popup : undefined,
				componentId : isdefined ? actionSource.componentId : undefined,
				componentAction : isdefined ? actionSource.componentAction : undefined
			};
			
			if(iswindow) {
				action.execute = angular.bind(action, function() {
					showWindow(this.windowId, this.popup);
				});
			} else if(iscore) {
				action.execute = angular.bind(action, function() {
					net.sendAction(this.owner.owner.id, this.owner.id, isprimary);
				});
			} else { // not defined
				action.execute = angular.bind(action, function() {
					// nothing
				});
			}
			
			return action;
		};
		
		/**
		 * Construction du composant du modèle avec le tableau d'images
		 */
		var componentBuilder = function(componentSource, owner, imageMap) {
			
			var component = {
				__component : componentSource,
				owner : owner,
				id : componentSource.id,
				displayName : componentSource.displayName,
				staticIcon : imageMap['component/' + componentSource.id + '/staticIconId'],
				defaultIcon : imageMap['component/' + componentSource.id + '/defaultIconId'],
				iconMap : {},
			};
			
			// Mapping des icones possibles du composant si icon dynamique
			var mappings = componentSource.icon.mappings;
			if(mappings != undefined && mappings != null) {
				for(var i=0, len=mappings.length; i<len; i++) {
					var mapping = mappings[i];
					var image = imageMap['component/' + component.id + '/mappings/' + i + '/iconId'];
					component.iconMap[mapping.iconId] = image;
				}
			}
			
			// actions
			component.primaryAction = actionBuilder(componentSource.primaryAction, component, true);
			component.secondaryAction = actionBuilder(componentSource.secondaryAction, component, false);
			
			component.mouseDown = angular.bind(mouseManager, mouseManager.componentMouseDown, component);
			component.mouseUp = angular.bind(mouseManager, mouseManager.componentMouseUp, component);
			
			// Définition de l'icone actuelle
			if(component.staticIcon != null && component.staticIcon != undefined)
				component.image = component.staticIcon;
			
			if(component.defaultIcon != null && component.defaultIcon != undefined)
				component.image = component.defaultIcon;
			
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
			if(componentsSource != null && componentsSource != undefined) {
				for(var i=0, len=componentsSource.length; i<len; i++) {
					var componentSource = componentsSource[i];
					var component = componentBuilder(componentSource, window, imageMap);
					window.components.push(component);
				}
			}
				
			return window;
		};
		
		/*
		 * Abonnement aux événements de net
		 */
		var registerNetEvents = function(window) {
		
			var lookupComponent = function(windowId, componentId) {
				// Le modèle courant ne gère qu'une seule fenêtre
				// Si l'événement ne concerne pas cette fenêtre, il n'y a rien à faire
				if(window.id != windowId)
					return null;
				for(var i=0, len=window.components.length; i<len; i++) {
					var component = window.components[i];
					if(component.id == componentId)
						return component;
				}
				// non trouvé
				return null;
			};
			
			var setDefaultIcon = function(component) {
				
				// Si icone statique rien à faire
				if(component.staticIcon != null && component.staticIcon != undefined)
					return;
				
				// Sinon on met l'icone par défaut (et null si l'icone n'est pas définie)
				$log.debug('setting image for window : ' + component.owner.id + ', component : ' + component.id + ' to default');
				component.image = component.defaultIcon;
			};
			
			net.onReceiveIcon(function(windowId, componentId, imageId) {
				var component = lookupComponent(windowId, componentId);
				if(component == null)
					return;

				// Si icone statique rien à faire
				if(component.staticIcon != null && component.staticIcon != undefined)
					return;
				
				if(imageId == null) {
					setDefaultIcon(component);
					return;
				}
				
				$log.debug('setting image for window : ' + windowId + ', component : ' + componentId + ' to : ' + imageId);
				component.image = component.iconMap[imageId];
			});
			
			net.onReceiveOnlineChanged(function(windowId, componentId, online) {
				// Sur online rien à faire, on attend l'icone à définir dans un prochain message
				if(online)
					return;

				var component = lookupComponent(windowId, componentId);
				if(component == null)
					return;
				
				setDefaultIcon(component);
			});
		};
		
		/**
		 * Fabrique
		 */
		return function($scope, window) {
			
			var keys = {};
			imageKeys(window, keys);
			imagesBatch(keys).then(function(imageMap) {
				var windowModel = builder(window, imageMap);
				registerNetEvents(windowModel);
				$scope.window = windowModel;
			});
		};
	}]);
});
