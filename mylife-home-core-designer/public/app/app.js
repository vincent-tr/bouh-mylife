/**
 * Gestion de l'application
 */

// https://github.com/mrquincle/jsplumb-example

'use strict';

var module = angular.module('mylife.app', ['mylife.component.designer', 'mylife.ui.designer', 'mylife.tools']);

module.run(['tools', function(tools) {
	tools.setAppTitle('mylife-home-core-designer');
}]);