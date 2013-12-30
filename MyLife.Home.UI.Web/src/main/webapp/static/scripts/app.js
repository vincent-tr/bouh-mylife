/**
 * Gestion de l'application
 */

var app = angular.module('mylife.app', ['ngRoute', 'mylife.controllers', 'mylife.structure']);

app.config(['$routeProvider', '$structure',
            function($routeProvider, $structure) {
	$routeProvider.
		when('/:windowId', {
			controller: 'windowController'
		}).
		otherwise({
			redirectTo: '/' + $structure.defaultWindowId()
		});
}]);