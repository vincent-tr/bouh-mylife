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
	
	var arrayFind = function(array, predicate) {
		for(var i=0, l=array.length; i<l; i++) {
			var item = array[i];
			if(predicate(item)) {
				return item;
			}
		}
		return null;
	};
	
	var arraySelect = function(array, transform) {
		var ret = [];
		for(var i=0, l=array.length; i<l; i++) {
			var item = array[i];
			ret.push(transform(item));
		}
		return ret;
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
		arrayFind: arrayFind,
		arraySelect: arraySelect,
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

module.directive('splitter', ['$timeout', '$window', '$rootScope', function($timeout, $window, $rootScope) {
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
					resize: function(event, ui) {
						resizeRight();
					}
				});

				$timeout(function(){
					resizeRight();
				}, 100); // TODO : better
				
				angular.element($window).bind('resize', function() {
					resizeRight();
				});
				
				$rootScope.$on('tabSelect', function() {
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
					resize:  function(event, ui) {
						resizeBottom();
					}
				});
				
				$timeout(function(){
					resizeBottom();
				}, 100); // TODO : better
				
				angular.element($window).bind('resize', function(event) {
					resizeBottom();
				});
				
				$rootScope.$on('tabSelect', function() {
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

module.directive('sglclick', ['$parse', function($parse) {
	return {
		restrict: 'A',
		link: function(scope, element, attr) {
			var fn = $parse(attr.sglclick);
			var delay = 300, clicks = 0, timer = null;

			element.on('click', function (event) {
				clicks++; //count clicks
				if(clicks === 1) {
					timer = setTimeout(function() {
						scope.$apply(function () {
							fn(scope, { $event: event });
						});
						clicks = 0; //after action performed, reset counter
					}, delay);
				} else {
					clearTimeout(timer); //prevent single-click action
					clicks = 0; //after action performed, reset counter
				}
			});
		}
    };
}]);