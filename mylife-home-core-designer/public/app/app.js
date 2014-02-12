/**
 * Gestion de l'application
 */

// https://github.com/mrquincle/jsplumb-example

'use strict';

var app = angular.module('mylife.app', ['mylife.api'/*, 'ui.bootstrap'*/]);

app.factory('plumbHelper', function() {
	
	var epPaintStyle = { 
			strokeStyle:"#7AB02C",
			fillStyle:"transparent",
			radius:4,
			lineWidth:3 
	};
	
	var epHoverPaintStyle = {
			strokeStyle:"#7AB02C",
			fillStyle:"black",
			radius:4,
			lineWidth:3 
	};
	
	return {
		makeSource: function(element) {
			jsPlumb.makeSource(element, {
				//endpoint:["Dot", {radius: 5}],
				anchor:[ ["Left"], ["Right"] ],
				maxConnections: -1,
				/*connector: [ "Flowchart", { stub:[40, 60], gap:10, cornerRadius:5, alwaysRespectStubs:true } ],*/
				paintStyle: epPaintStyle,
				hoverPaintStyle: epHoverPaintStyle,
				connectorPaintStyle: {
					lineWidth:4,
					strokeStyle:"#216477",
				},
				connectorHoverStyle: {
					lineWidth:4,
					strokeStyle:"#216477",
				},
			});
		},
		
		makeTarget: function(element) {
			jsPlumb.makeTarget(element, {
				//endpoint:["Dot", {radius: 5}],
				anchor:[ ["Left"], ["Right"] ],
				maxConnections: -1,
				paintStyle: epPaintStyle,
				hoverPaintStyle: epHoverPaintStyle,
			});
		}
	};
});

app.controller('designerController', ['$scope', 'api', function($scope, api) {

	$scope.pluginTypes = [];
	$scope.plugins = [];
	$scope.hardware = [];

	var applyData = function(data) {

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
					plugin.internal.type = type;
					break;
				}
			}
		};
		
		jsPlumb.detachEveryConnection();
		$scope.pluginTypes = [];
		$scope.plugins = [];
		$scope.hardware = [];
		$scope.links = [];
	
		$scope.pluginTypes = data.pluginTypes;
		$scope.plugins = data.plugins;
		$scope.hardware = data.hardware;
		$scope.links = data.links;

		$scope.plugins.forEach(attachTypeToPlugin);
		
		$scope.plugins.forEach(checkDesignerData);
		$scope.hardware.forEach(checkDesignerData);
		
		// TODO : liens
	};
	
	$scope.reload = function() {
		api.data.get({}, function(data) {
			applyData(data);
		});
	};
	
	$scope.updateHardware = function(url) {
		// on envoie l'url et on r�cup�re une version du sch�ma avec le nouveau mat�riel dessus (non sauvegard�)
		api.updateHardware.post({}, {url: url}, function(data) {
			applyData(data);
		});
	};
	
	$scope.save = function() {
		
		var removeInternal = function(item) {
			if(item.internal) {
				delete item.internal;
			}
		};
		
		var clone = function(obj) {
			return JSON.parse(JSON.stringify(obj));
		};
		
		var prepareArray = function(source) {
			var dest = clone(source);
			dest.forEach(removeInternal);
		};
		
		var data = {
			plugins: prepareArray($scope.plugins),
			hardware: prepareArray($scope.hardware),
			links: prepareArray($scope.links)
		};
		
		api.merge.post({}, data, function(data) {
			// TODO
		});
	};
	
	$scope.createPlugin = function(typeId, x, y) {
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

/**
 * This directive should allow an element to be dragged onto the main canvas. Then after it is dropped, it should be
 * painted again on its original position, and the full module should be displayed on the dragged to location.
 */
app.directive('toolboxItem', function() {
	return {
		replace: true,
		controller: 'designerController',
		link: function (scope, element, attrs) {

			// jsPlumb uses the containment from the underlying library, in our case that is jQuery.
			jsPlumb.draggable(element, {
				revert: true,
				//helper: 'clone',
				zIndex: 100,
				containment: $('#main')
			});
		}
	};
});

//directives link user interactions with $scope behaviours
//now we extend html with <div plumb-item>, we can define a template <> to replace it with "proper" html, or we can 
//replace it with something more sophisticated, e.g. setting jsPlumb arguments and attach it to a double-click 
//event
app.directive('schemaItem', function() {
	return {
		replace: true,
		controller: 'designerController',
		link: function (scope, element, attrs) {

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

app.directive('componentAttribute', ['plumbHelper', function(plumbHelper) {
	return {
		replace: true,
		controller: 'designerController',
		link: function (scope, element, attrs) {
			plumbHelper.makeSource(element);
		}
	};
}]);


app.directive('componentAction', ['plumbHelper', function(plumbHelper) {
	return {
		replace: true,
		controller: 'designerController',
		link: function (scope, element, attrs) {
			plumbHelper.makeTarget(element);
		}
	};
}]);

app.directive('schemaContainer', function($compile) {
	return {
		restrict: 'A',
		link: function(scope, element, attrs){

			element.droppable({
				drop: function(event,ui) {
					var typeId = angular.element(ui.draggable).data('identifier'),
					dragElement = angular.element(ui.draggable),
					dropElement = angular.element(this);

					// if dragged item has class menu-item and dropped div has class drop-container, add module 
					if (dragElement.hasClass('toolbox-item') && dropElement.hasClass('drop-container')) {
						var x = event.pageX - dropElement.offset().left;
						var y = event.pageY - dropElement.offset().top;

						scope.createPlugin(typeId, x, y);
					}

					scope.$apply();
				}
			});
		}
	};
});

app.directive('splitter', ['$timeout', function($timeout) {
	return {
		restrict:'A',
		link: function(scope, element, attrs) {
			
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
				minWidth: '100',
				maxWidth: '500',
				resize: resizeRight
			});
			
			$timeout(function(){
				resizeRight();
			});
		}
	};
}]);
