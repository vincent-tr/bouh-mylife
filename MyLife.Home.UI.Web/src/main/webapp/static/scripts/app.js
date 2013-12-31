/**
 * Gestion de l'application
 */

'use strict';

var app = angular.module('mylife.app', ['ngRoute', 'mylife.controllers', 'mylife.structure', 'mylife.urlHelper']);

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
				resolve : { 'window' : ['$route', function($route) {
					return structure.getWindow($route.current.params.windowId);
				}]}
			}).
			otherwise({
				redirectTo : '/' + windowId
			});
		
		$route.reload();
	});
}]);
