/**
 * Gestion de l'application
 */

'use strict';

var module = angular.module('mylife.app', ['ngRoute', 'mylife.tools', 'mylife.net']);

module.run(['tools', function(tools) {
	tools.setAppTitle('mylife-home-ui');
}]);

module.controller('controller', ['$scope', 'net', function($scope, net) {
	$scope.connected = function() { return net.connected; };
}]);

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
