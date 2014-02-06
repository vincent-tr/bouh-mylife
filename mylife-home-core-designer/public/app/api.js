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
		updateHardware: $resource('/updateHardware', {}, {
			post: { method: 'POST' }
		}),
		merge: $resource('/merge', {}, {
			post: { method: 'POST' }
		}),
		apply: $resource('/apply', {}, {
			post: { method: 'POST' }
		})
	};
}]);

