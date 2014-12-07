/**
 * Générateur d'ids
 */

'use strict';

var module = angular.module('mylife.idGenerator', ['mylife.tools']);

module.factory('idGenerator', [function() {
	
	var last_id = 0;
	
	return function() {
		return ++last_id;
	};
}]);