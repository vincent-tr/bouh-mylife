/**
 * Outils
 */

'use strict';

var app = angular.module('mylife.tools', ['ui.bootstrap']);

app.factory('mylife.tools.dialog.confirm', ['$modal', function($modal) {
	
	var checkParam = function(param, defaultValue) {
		if(param)
			return param;
		return defaultValue;
	};
	
	return function (params) {

		var modalInstance = $modal.open({
			templateUrl: 'templates/confirm.html',
			controller: function ($scope, $modalInstance) {
				$scope.title = checkParam(params.title, 'title');
				$scope.text = checkParam(params.text, 'text');
				$scope.labelOk = checkParam(params.labelOk, 'Ok');
				$scope.labelCancel = checkParam(params.labelCancel, 'Annuler');

				$scope.ok = function () {
					$modalInstance.close();
				};

				$scope.cancel = function () {
					$modalInstance.dismiss();
				};
			}
		});

		modalInstance.result.then(function () {
			if(params.callbackOk)
				params.callbackOk();
		}, function () {
			if(params.callbackCancel)
				params.callbackCancel();
		});
	};
}]);