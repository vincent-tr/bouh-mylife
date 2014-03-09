/**
 * Gestion de l'application
 */

'use strict';

var module = angular.module('mylife.app', ['mylife.tools', 'mylife.net']);

module.run(['tools', function(tools) {
	tools.setAppTitle('mylife-home-ui');
}]);

module.controller('controller', ['$scope', 'net', function($scope, net) {
	$scope.connected = function() { return net.connected; };
}]);