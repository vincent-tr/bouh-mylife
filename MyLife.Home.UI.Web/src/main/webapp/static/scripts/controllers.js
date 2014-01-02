/**
 * Gestion des contrôleurs
 */

'use strict';

var controllers = angular.module('mylife.controllers', ['mylife.modelBuilder', 'mylife.net']);

controllers.controller('windowController', 
		['$scope', '$log', 'modelBuilder', 'net', 'window', 'popup',
		 function ($scope,$log, modelBuilder, net, window, popup) {
	$log.debug('showing window : ' + window.id + ' (popup : ' + popup + ')');
	
	// Vidage de la liste de fenêtres si on n'est pas une popup,
	// en effet sinon il reste les fenêtres affichés dans la vue précédente
	if(!popup)
		net.windowClear();
	
	net.windowPush(window.id);
	modelBuilder($scope, window);
}]);
