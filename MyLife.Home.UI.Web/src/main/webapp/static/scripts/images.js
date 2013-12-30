/**
 * Gestion d'obtention des images
 */


angular.module('mylife.images', [], function($provide) {
	$provide.factory('$images', ['$log', '$structure', function($log, $structure) {
		return function($imageId) {
			// TODO : gérer image par défaut
			return $structure.getImage($imageId);
		};
	}]);
});