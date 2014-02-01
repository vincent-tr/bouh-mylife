/**
 * New node file
 */

'use strict';

var api = angular.module('mylife.api', ['ngResource']);

api.factory('pluginTypes', ['$resource', function($resource) {
	return $resource('/core/api/pluginTypes', {}, {
		get: { method: 'GET' },
		add: { method: 'POST' }
	});
}]);

api.factory('plugins', ['$resource', function($resource) {
	return $resource('/core/api/plugins', {}, {
		get: { method: 'GET' },
		add: { method: 'POST' },
		del: { method: 'DELETE', url: '/core/api/plugins/:id' }
	});
}]);

api.factory('hardware', ['$resource', function($resource) {
	return $resource('/core/api/hardware', {}, {
		get: { method: 'GET' },
		add: { method: 'POST' },
		del: { method: 'DELETE', url: '/core/api/hardware/:id' }
	});
}]);

api.factory('links', ['$resource', function($resource) {
	return $resource('/core/api/links', {}, {
		get: { method: 'GET' },
		add: { method: 'POST' },
		del: { method: 'DELETE', url: '/core/api/links/:id' }
	});
}]);

api.factory('ui', ['$resource', function($resource) {
	return $resource('/core/api/ui', {}, {
		get: { method: 'GET' },
		add: { method: 'POST' }
	});
}]);
