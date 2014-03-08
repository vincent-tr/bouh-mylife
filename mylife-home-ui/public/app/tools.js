/**
 * Outils
 */

'use strict';

var module = angular.module('mylife.tools', ['ui.bootstrap']);

module.factory('tools', [function() {
	
	var removeFromArray = function(array, item) {
		var index = array.indexOf(item);
		if(index < 0)
			return false;
		array.splice(index, 1);
		return true;
	};
	
	var checkParam = function(param, defaultValue) {
		if(param)
			return param;
		return defaultValue;
	};
	
	var attachInternal = function(item) {
		if(!item.internal) {
			
			var internalObject = {};
			
			item.internal = function() {
				return internalObject;
			};
		}
	};
	
	var clone = function(obj) {
		return JSON.parse(JSON.stringify(obj));
	};
	
	var appTitle = '';
	
	var findScopeAncestor = function(scope, id) {
		while(scope) {
			if(scope.$id === id) {
				return scope;
			}
			
			scope = scope.$parent;
		}
		
		return null;
	};
	
	return {
		removeFromArray: removeFromArray,
		checkParam: checkParam,
		attachInternal: attachInternal,
		clone: clone,
		getAppTitle: function() { return appTitle; },
		setAppTitle: function(title) { appTitle = title; },
		findScopeAncestor: findScopeAncestor
	};
}]);

module.factory('dialogConfirm', ['$modal', 'tools', function($modal, tools) {
	
	return function (params) {

		var modalInstance = $modal.open({
			templateUrl: 'templates/confirm.html',
			controller: function ($scope, $modalInstance) {
				$scope.title = tools.checkParam(params.title, tools.getAppTitle());
				$scope.text = tools.checkParam(params.text, '');
				$scope.labelOk = tools.checkParam(params.labelOk, 'Ok');
				$scope.labelCancel = tools.checkParam(params.labelCancel, 'Annuler');

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
			if(params.callbackOk)
				params.callbackOk();
		});
	};
}]);

module.factory('dialogAlert', ['$modal', 'tools', function($modal, tools) {
	
	return function(params) {
		
		var modalInstance = $modal.open({
			templateUrl: 'templates/alert.html',
			controller: function ($scope, $modalInstance) {
				$scope.title = tools.checkParam(params.title, tools.getAppTitle());
				$scope.text = tools.checkParam(params.text, '');
				$scope.labelOk = tools.checkParam(params.labelOk, 'Ok');

				$scope.ok = function () {
					$modalInstance.close();
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

module.factory('dialogPrompt', ['$modal', 'tools', function($modal, tools) {
	
	return function (params) {

		var modalInstance = $modal.open({
			templateUrl: 'templates/prompt.html',
			controller: function ($scope, $modalInstance) {
				$scope.value = tools.checkParam(params.defaultValue, '');
				$scope.title = tools.checkParam(params.title, tools.getAppTitle());
				$scope.text = tools.checkParam(params.text, '');
				$scope.labelOk = tools.checkParam(params.labelOk, 'Ok');
				$scope.labelCancel = tools.checkParam(params.labelCancel, 'Annuler');

				$scope.ok = function () {
					$modalInstance.close($scope.value);
				};

				$scope.cancel = function () {
					$modalInstance.dismiss();
				};
			}
		});

		modalInstance.result.then(function (value) {
			if(params.callbackOk)
				params.callbackOk(value);
		}, function () {
			if(params.callbackCancel)
				params.callbackCancel();
		});
	};
}]);

module.directive('splitter', ['$timeout', '$window', function($timeout, $window) {
	return {
		restrict:'A',
		link: function(scope, element, attrs) {
			
			var verticalSplitter = function(min, max) {
				var jqLeft = $(element);
				var jqRight = jqLeft.next();
				var jqContainer = jqLeft.parent();
				
				var resizeRight = function() {
					var remainingSpace = jqContainer.width() - jqLeft.outerWidth();
					var rightPos = jqLeft.outerWidth();
					var rightWidth = remainingSpace - (jqRight.outerWidth() - jqRight.width());
					jqRight.width(rightWidth);
					jqRight.css({left:rightPos,top:0});
				};
				
				jqLeft.resizable({
					handles: 'e',
					minWidth: min,
					maxWidth: max,
					resize: resizeRight
				});

				$timeout(function(){
					resizeRight();
				}, 100); // TODO : better
				
				angular.element($window).bind('resize', function() {
					resizeRight();
				});
			};
			
			var horizontalSplitter = function(min, max) {
				var jqTop = $(element);
				var jqBottom = jqTop.next();
				var jqContainer = jqTop.parent();
				
				var resizeBottom = function() {
					var remainingSpace = jqContainer.height() - jqTop.outerHeight();
					var bottomPos = jqTop.outerHeight();
					var bottomHeight = remainingSpace - (jqBottom.outerHeight() - jqBottom.height());
					jqBottom.height(bottomHeight);
					jqBottom.css({top:bottomPos,left:0});
				};
				
				jqTop.resizable({
					handles: 's',
					minHeight: min,
					maxHeight: max,
					resize: resizeBottom
				});
				
				$timeout(function(){
					resizeBottom();
				}, 100); // TODO : better
				
				angular.element($window).bind('resize', function() {
					resizeBottom();
				});
			};
			
			var options = attrs.splitter;
			if(options) {
				options = eval('(' + options + ')');
			}
			
			var direction = undefined;
			if(options) {
				direction = options.direction;
			}
			if(direction !== 'horizontal' && direction !== 'vertical')
				direction = 'vertical';
			
			var min = undefined;
			if(options) {
				min = options.min;
			}
			var max = undefined;
			if(options) {
				max = options.max;
			}
			
			if(direction === 'vertical') {
				verticalSplitter(min, max);
			} else {
				horizontalSplitter(min, max);
			}
		}
	};
}]);

module.directive('initializer', [ '$timeout', function($timeout) {
	return {
		restrict : 'A', 
		terminal : true,
		transclude : true,
		link : function(scope, element, attrs) {
			$timeout(scope.init, 0);
		}
	};
}]);
