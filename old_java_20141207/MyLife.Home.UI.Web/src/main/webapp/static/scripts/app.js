/**
 * Gestion de l'application
 */

'use strict';

var app = angular.module('mylife.app', ['ngRoute', 'ui.bootstrap', 'mylife.controllers', 'mylife.structure', 'mylife.urlHelper', 'mylife.net']);

app.config(['$provide', '$routeProvider', function($provide, $routeProvider) {
	$provide.factory('$routeProvider', function() {
		return $routeProvider;
	});
}]);

app.run(['$routeProvider', '$route', 'urlHelper', 'structure', function($routeProvider, $route, urlHelper, structure) {

	structure.getDefaultWindowId().then(function(windowId) {
		$routeProvider.
			when('/:windowId', {
				controller : 'windowController',
				templateUrl : urlHelper.partial('window.html'),
				resolve : {
					'window' : ['$route', function($route) { return structure.getWindow($route.current.params.windowId);}],
					'popup' : function() { return false; }
				}
			}).
			otherwise({
				redirectTo : '/' + windowId
			});
		
		$route.reload();
	});
}]);

app.factory('showWindow', ['$modal', '$location', 'urlHelper', 'structure', 'net', function($modal, $location, urlHelper, structure, net) {
	
	var showPopup = function(windowId) {
		
		var modalInstance = $modal.open({
			controller : 'windowController',
			templateUrl : urlHelper.partial('window.html'),
			resolve : {
				'window' : function() { return structure.getWindow(windowId); },
				'popup' : function() { return true; }
			}
		});
		
		var popupClosed = function() {
			// On enlève la popup de la liste de fenêtres a afficher
			net.windowPop();
		};
		
		modalInstance.result.then(popupClosed, popupClosed);
		
	};
	
	return function(windowId, popup) {
		if(popup) {
			showPopup(windowId);
		} else {
			$location.path('/' + windowId);
		}
	};
}]);

// http://stackoverflow.com/questions/14507918/how-to-access-image-properties-after-angular-ng-repeat
app.directive('mylifeResizeParent', function(){ 
	return {
		restrict: 'A',
		link: function(scope, elem, attrs) {
			elem.on('load', function() {
				var w = this.width,
					h = this.height;

				var parent = elem.parent()[0];

				// Application au parent
				parent.style.width = w;
				parent.style.height = h;
			});
		}
	};
});

app.directive('mylifeComponentPosition', function() {
	return function(scope, element, attrs) {
		
		var updatePosition = function() {
			// Récupération des données
			var component = scope.component;
			var componentContainer = element[0];
			var container = element.parent()[0];
			
			var componentWidth = componentContainer.offsetWidth;			
			var componentHeight = componentContainer.offsetHeight;			
			var containerWidth = container.clientWidth;
			var containerHeight = container.clientHeight;
			var componentX = 0;
			var componentY = 0;
			if(component != null && component != undefined) {
				componentX = component.positionX;
				componentY = component.positionY;
			}
			
			// Détermination de la position
			var baseX = (containerWidth - componentWidth) / 2;
			var x = baseX + (baseX * componentX);
			var baseY = (containerHeight - componentHeight) / 2;
			var y = baseY + (baseY * componentY);
			
			componentContainer.style.left = x;
			componentContainer.style.top = y;
		};
		
		element.ready(function() {
			updatePosition();
		});
	};
});
