/**
 * New node file
 */

'use strict';

var api = angular.module('mylife.api', ['ngResource']);

api.factory('api', ['$resource', function($resource) {
	return {
		all:  $resource('/core/api/all', {}, {
			get: { method: 'GET' }
		}),
		pluginTypes: $resource('/core/api/pluginTypes', {}, {
			get: { method: 'GET' },
			add: { method: 'POST' }
		}),
		plugins: $resource('/core/api/plugins', {}, {
			get: { method: 'GET' },
			add: { method: 'POST' },
			del: { method: 'DELETE', url: '/core/api/plugins/:id' }
		}),
		hardware: $resource('/core/api/hardware', {}, {
			get: { method: 'GET' },
			add: { method: 'POST' },
			del: { method: 'DELETE', url: '/core/api/hardware/:id' }
		}),
		links: $resource('/core/api/links', {}, {
			get: { method: 'GET' },
			add: { method: 'POST' },
			del: { method: 'DELETE', url: '/core/api/links/:id' }
		})
	};
}]);

api.factory('api.all', ['$resource', function($resource) {
	return $resource('/core/api/all', {}, {
		get: { method: 'GET' }
	});
}]);

api.factory('api.pluginTypes', ['$resource', function($resource) {
	return $resource('/core/api/pluginTypes', {}, {
		get: { method: 'GET' },
		add: { method: 'POST' }
	});
}]);

api.factory('api.plugins', ['$resource', function($resource) {
	return $resource('/core/api/plugins', {}, {
		get: { method: 'GET' },
		add: { method: 'POST' },
		del: { method: 'DELETE', url: '/core/api/plugins/:id' }
	});
}]);

api.factory('api.hardware', ['$resource', function($resource) {
	return $resource('/core/api/hardware', {}, {
		get: { method: 'GET' },
		add: { method: 'POST' },
		del: { method: 'DELETE', url: '/core/api/hardware/:id' }
	});
}]);

api.factory('api.links', ['$resource', function($resource) {
	return $resource('/core/api/links', {}, {
		get: { method: 'GET' },
		add: { method: 'POST' },
		del: { method: 'DELETE', url: '/core/api/links/:id' }
	});
}]);

api.factory('api.ui', ['$resource', function($resource) {
	return $resource('/core/api/ui', {}, {
		get: { method: 'GET' },
		add: { method: 'POST' }
	});
}]);
