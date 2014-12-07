/**
 * Gestion d'obtention des images
 */
'use strict';

angular.module('mylife.images', ['mylife.structure'], function($provide) {
	$provide.factory('images', ['$log', 'structure', function($log, structure) {
		return function(imageId) {
			// TODO : gérer image par défaut
			return structure.getImageData(imageId).then(function(imageData) {
				if(imageData == null)
					return null;
				// TODO : gérer d'autres types d'images
				return 'data:image/png;base64,' + imageData;
			});
		};
	}]);
});