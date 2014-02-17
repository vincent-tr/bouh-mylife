/**
 * Outils
 */

'use strict';

var app = angular.module('mylife.tools', ['ui.bootstrap']);

app.factory('mylife.tools.dialog.confirm', ['$modal', function($modal) {
	
	$scope.open = function (params) {

	    var modalInstance = $modal.open({
	      templateUrl: 'templates/confirm.html',
	      controller: function ($scope, $modalInstance) {

	    	  $scope.title = 'title';
	    	  $scope.text = 'text';
	    	  $scope.labelOk = 'Ok';
	    	  $scope.labelCancel = 'Annuler';

	    	  $scope.ok = function () {
	    	    $modalInstance.close();
	    	  };

	    	  $scope.cancel = function () {
	    	    $modalInstance.dismiss();
	    	  };
	    	}
	    });

	    modalInstance.result.then(function () {
	      $scope.selected = selectedItem;
	    }, function () {
	      $log.info('Modal dismissed at: ' + new Date());
	    });
	
}]);