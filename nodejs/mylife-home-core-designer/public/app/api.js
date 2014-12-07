/**
 * New node file
 */

'use strict';

var module = angular.module('mylife.api', ['ngResource']);

module.factory('api', ['$resource', function($resource) {
	return {
		component: {
			data: $resource('/component/data', {}, {
				get: { method: 'GET' }
			}),
			hardware: $resource('/component/hardware', {}, {
				post: { method: 'POST' }
			}),
			merge: $resource('/component/merge', {}, {
				post: { method: 'POST' }
			}),
			apply: $resource('/component/apply', {}, {
				post: { method: 'POST' }
			})
		},
		ui: {
			data: $resource('/ui/data', {}, {
				get: { method: 'GET' },
				post: { method: 'POST' }
			}),
			components: $resource('/ui/components', {}, {
				get: { method: 'GET' }
			})
		}
	};
}]);
