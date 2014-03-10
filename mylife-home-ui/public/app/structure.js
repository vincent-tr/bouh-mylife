/**
 * Structure
 */

'use strict';

var module = angular.module('mylife.structure', ['ngResource']);

module.factory('structure', ['$resource', function($resource) {
	return {
		data: $resource('/structure', {}, {
			get: { method: 'GET' }
		})
	};
}]);
