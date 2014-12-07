/**
 * Structure
 */

'use strict';

var module = angular.module('mylife.api', ['ngResource']);

module.factory('api', ['$resource', function($resource) {
	return {
		structure: $resource('/structure', {}, {
			get: { method: 'GET' }
		})
	};
}]);
