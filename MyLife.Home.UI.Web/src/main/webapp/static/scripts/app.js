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