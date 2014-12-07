/**
 * Gestion d'obtention des données de structure
 */

'use strict';

angular.module('mylife.structure', ['mylife.urlHelper'], function($provide) {
	$provide.factory('structure', ['$log', '$http', 'urlHelper', function($log, $http, urlHelper) {
	
		var dataPromise = null;
		
		var getData = function() {
		    if (dataPromise == null) {
		    	$log.debug('loading structure');
				var url = urlHelper.servlet('structure');
		        dataPromise = $http.get(url).then(
	        		function(res) {
	        			$log.debug('structure loaded');
	        			return res.data;
	        		}, 
	        		function() {
						$log.error('Error loading structure');
	        		});
		    }
		    return dataPromise;
		};
		
		var getWindow = function(windowId) {
			return getData().then(function(data) {
				var windows = data.windows;
				if(windows == undefined)
					return null;
				
				for(var i=0, len=windows.length; i<len; i++) {
					var window = windows[i];
					if(window.id == windowId)
						return window;
				}
				return null;
			});
		};
		
		var getImageData = function(imageId) {
			return getData().then(function(data) {
				var images = data.images;
				if(images == undefined)
					return null;
				
				for(var i=0, len=images.length; i<len; i++) {
					var image = images[i];
					if(image.id == imageId)
						return image.content;
				}
				return null;
			});
		};
		
		var getDefaultWindowId = function() {
			return getData().then(function(data) {
				return data.defaultWindowId;
			});
		};
		
		var reset = function() {
			dataPromise = null;
		};
		
		return {
			getWindow : getWindow,
			getImageData : getImageData,
			getDefaultWindowId : getDefaultWindowId,
			reset : reset,
		};
	}]);
});