/**
 * Gestion de l'application
 */

'use strict';

var module = angular.module('mylife.app', ['mylife.tools']);

module.run(['tools', function(tools) {
	tools.setAppTitle('mylife-home-ui');
}]);
