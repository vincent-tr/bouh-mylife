/**
 * Gestion de l'application
 */

// https://github.com/mrquincle/jsplumb-example

'use strict';

var app = angular.module('mylife.app', ['mylife.api'/*, 'ui.bootstrap'*/]);

app.controller('designerController', ['$scope', 'api', function($scope, api) {

	$scope.pluginTypes = [];
	$scope.plugins = [];
	$scope.hardware = [];

	$scope.reload = function() {
		jsPlumb.detachEveryConnection();
		$scope.pluginTypes = [];
		$scope.plugins = [];
		$scope.hardware = [];
		
		var checkDesignerData = function(item) {
			var designer = item.designer;
			if(!designer) {
				item.designer = designer = {
					x: 0,
					y: 0
				};
			}
		};
		
		var attachTypeToPlugin = function(plugin) {
			
			if(!plugin.internal) {
				plugin.internal = {};
			}
			
			for (var i = 0, l = $scope.pluginTypes.length; i < l; i++) {
				var type = $scope.pluginTypes[i];
				if (type.id === plugin.type) {
					// à virer au merge
					plugin.internal.type = type;
					break;
				}
			}
		};
		
		api.data.get({}, function(data) {
			$scope.pluginTypes = data.pluginTypes;
			$scope.plugins = data.plugins;
			$scope.hardware = data.hardware;

			$scope.plugins.forEach(attachTypeToPlugin);
			
			$scope.plugins.forEach(checkDesignerData);
			$scope.hardware.forEach(checkDesignerData);
		});
	};
	
	$scope.createPlugin = function(typeId) {
		// TODO
	};

	$scope.removeState = function(schema_id) {
		console.log("Remove state " + schema_id + " in array of length " + $scope.schema.length);
		for (var i = 0; i < $scope.schema.length; i++) {
			// compare in non-strict manner
			if ($scope.schema[i].schema_id == schema_id) {
				console.log("Remove state at position " + i);
				$scope.schema.splice(i, 1);
			}
		}
	};
}]);

app.directive('postRender', [ '$timeout', function($timeout) {
	var def = {
			restrict : 'A', 
			terminal : true,
			transclude : true,
			link : function(scope, element, attrs) {
				$timeout(scope.reload, 0);  //Calling a scoped method
			}
	};
	return def;
}]);


//directives link user interactions with $scope behaviours
//now we extend html with <div plumb-item>, we can define a template <> to replace it with "proper" html, or we can 
//replace it with something more sophisticated, e.g. setting jsPlumb arguments and attach it to a double-click 
//event
app.directive('plumbItem', function() {
	return {
		replace: true,
		controller: 'designerController',
		link: function (scope, element, attrs) {
			console.log("Add plumbing for the 'item' element");

			jsPlumb.makeTarget(element, {
				anchor: 'Continuous',
				maxConnections: 2,
			});
			jsPlumb.draggable(element, {
				containment: 'parent'
			});

			// this should actually done by a AngularJS template and subsequently a controller attached to the dbl-click event
			element.bind('dblclick', function(e) {
				jsPlumb.detachAllConnections($(this));
				$(this).remove();
				// stop event propagation, so it does not directly generate a new state
				e.stopPropagation();
				//we need the scope of the parent, here assuming <plumb-item> is part of the <plumbApp>			
				scope.$parent.removeState(attrs.identifier);
				scope.$parent.$digest();
			});

		}
	};
});

/**
 * This directive should allow an element to be dragged onto the main canvas. Then after it is dropped, it should be
 * painted again on its original position, and the full module should be displayed on the dragged to location.
 */
app.directive('plumbMenuItem', function() {
	return {
		replace: true,
		controller: 'designerController',
		link: function (scope, element, attrs) {
			console.log("Add plumbing for the 'menu-item' element");

			// jsPlumb uses the containment from the underlying library, in our case that is jQuery.
			jsPlumb.draggable(element, {
				containment: element.parent().parent()
			});
		}
	};
});

app.directive('plumbConnect', function() {
	return {
		replace: true,
		link: function (scope, element, attrs) {
			console.log("Add plumbing for the 'connect' element");

			jsPlumb.makeSource(element, {
				parent: $(element).parent(),
//				anchor: 'Continuous',
				paintStyle:{ 
					strokeStyle:"#225588",
					fillStyle:"transparent",
					radius:7,
					lineWidth:2 
				},
			});

		}
	};
});

app.directive('droppable', function($compile) {
	return {
		restrict: 'A',
		link: function(scope, element, attrs){
			console.log("Make this element droppable");

			element.droppable({
				drop:function(event,ui) {
					// angular uses angular.element to get jQuery element, subsequently data() of jQuery is used to get
					// the data-identifier attribute
					var typeId = angular.element(ui.draggable).data('identifier'),
					dragEl = angular.element(ui.draggable),
					dropEl = angular.element(this);

					// if dragged item has class menu-item and dropped div has class drop-container, add module 
					if (dragEl.hasClass('menu-item') && dropEl.hasClass('drop-container')) {
						console.log("Drag event on " + dragIndex);
						var x = event.pageX - scope.module_css.width / 2;
						var y = event.pageY - scope.module_css.height / 2;

						scope.createPlugin(typeId, x, y);
					}

					scope.$apply();
				}
			});
		}
	};
});

app.directive('draggable', function() {
	return {
		// A = attribute, E = Element, C = Class and M = HTML Comment
		restrict:'A',
		//The link function is responsible for registering DOM listeners as well as updating the DOM.
		link: function(scope, element, attrs) {
			element.draggable({
				// let it go back to its original position
				revert:true,
			});
		}
	};
});