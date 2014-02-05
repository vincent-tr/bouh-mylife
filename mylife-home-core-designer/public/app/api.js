/**
 * New node file
 */

'use strict';

var api = angular.module('mylife.api', ['ngResource']);

api.factory('api', ['$resource', function($resource) {
	return {
		data:  $resource('/data', {}, {
			get: { method: 'GET' }
		}),
		merge: $resource('/merge', {}, {
			add: { method: 'POST' }
		}),
		apply: $resource('/apply', {}, {
			add: { method: 'POST' }
		})
	};
}]);

