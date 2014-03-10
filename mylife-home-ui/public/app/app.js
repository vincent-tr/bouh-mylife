/**
 * Gestion de l'application
 */

'use strict';

var module = angular.module('mylife.app', ['ngRoute', 'mylife.tools', 'mylife.net', 'mylife.api']);

module.run(['tools', function(tools) {
	tools.setAppTitle('mylife-home-ui');
}]);

module.controller('controller', ['$scope', 'net', function($scope, net) {
	$scope.connected = function() { return net.connected; };
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
					'structure' : structure,
					'windowId' : ['$route', function($route) { return $route.current.params.windowId; }],
					'popup' : false
				}
			}).
			otherwise({
				redirectTo : '/' + structure.defaultWindow
			});
		
		$route.reload();
	});
}]);
