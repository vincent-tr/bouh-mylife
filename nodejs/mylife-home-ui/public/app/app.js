/**
 * Gestion de l'application
 */

'use strict';

var module = angular.module('mylife.app', ['ngRoute', 'mylife.api', 'mylife.controllers']);

module.run(['tools', function(tools) {
	tools.setAppTitle('mylife-home-ui');
}]);

module.config(['$provide', '$routeProvider', function($provide, $routeProvider) {
	$provide.factory('$routeProvider', function() {
		return $routeProvider;
	});
}]);

module.run(['$routeProvider', '$route', 'api', function($routeProvider, $route, api) {

	api.structure.get({}, function(structure) {
		
		$routeProvider.
			when('/:windowId', {
				controller : 'windowController',
				templateUrl : 'window.html',
				resolve : {
					'structure' : function() { return structure; },
					'windowId' : ['$route', function($route) { return $route.current.params.windowId; }],
					'popup' : function() { return false; }
				}
			}).
			otherwise({
				redirectTo : '/' + structure.defaultWindow
			});
		
		$route.reload();
	});
}]);
