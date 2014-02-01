/**
 * Gestion de l'application
 */

// https://github.com/mrquincle/jsplumb-example

'use strict';

var app = angular.module('mylife.app', ['mylife.api'/*, 'ui.bootstrap'*/]);

app.controller('designerController', ['$scope', 'pluginTypes', 'plugins', function($scope, pluginTypes, plugins) {

	// define a module with library id, schema id, etc.
	function module(data, x, y) {
		this.data = data;
		this.x = x;
		this.y = y;
	}

	// module should be visualized by title, icon
	$scope.library = [];

	// state is [identifier, x position, y position, title, description]
	$scope.schema = [];

	// todo: find out how to go back and forth between css and angular
	$scope.library_topleft = {
			x: 15,
			y: 145,
			item_height: 50,
			margin: 5,
	};

	$scope.module_css = {
			width: 150,
			height: 100, // actually variable
	};
	
	$scope.library_pos = {
	};

	$scope.reload = function() {
		$scope.schema_uuid = 0;
		jsPlumb.detachEveryConnection();
		$scope.schema = [];
		$scope.library = [];
		
		$scope.library_pos = {
			x: $scope.library_topleft.x+$scope.library_topleft.margin,
			y: $scope.library_topleft.y+$scope.library_topleft.margin
		};

		pluginTypes.get({}, function(data) {
			for(var key in data) {
				 if (data.hasOwnProperty(key)) {
					 
					 var type = data[key];
					 
					 // toutes les définitions de plugins contiennent un sous-type plugin.
					 // si inexistant, c'est un truc rajouté par angular
					 if(type.plugin === undefined)
						 continue;
					 
					 $scope.addModuleToLibrary(type);
				}			
			}
		});
		
		plugins.get({}, function(data) {
			for(var key in data) {
				 if (data.hasOwnProperty(key)) {

					 var plugin = data[key];
					 
					 // toutes les définitions de plugins contiennent un sous-type plugin.
					 // si inexistant, c'est un truc rajouté par angular
					 if(plugin.id === undefined)
						 continue;
					 
					 $scope.addModuleToSchema(plugin);
				 }
			}
		});
	};

	// add a module to the library
	$scope.addModuleToLibrary = function(type) {
		
		console.log("Add module " + type.displayName + " to library");
		
		var m = new module(type, $scope.library_pos.x, $scope.library_pos.y);
		$scope.library.push(m);
		
		$scope.library_pos.y += $scope.library_topleft.item_height;
	};

	// add a module to the schema
	$scope.addModuleToSchema = function(plugin) {
		console.log("Add module " + plugin.id + " to schema");
		
		var library;
		for (var i = 0, l = $scope.library.length; i < l; i++) {
			if ($scope.library[i].data.id == plugin.typeId) {
				library = $scope.library[i];
			}
		}
		
		var designer = plugin.config.designer;
		if(!designer) {
			plugin.config.designer = designer = {
				x: 0,
				y: 0,
				library: library
			};
		}
		var m = new module(plugin, plugin, designer.x, designer.y);
		$scope.schema.push(m);
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
					var dragIndex = angular.element(ui.draggable).data('identifier'),
					dragEl = angular.element(ui.draggable),
					dropEl = angular.element(this);

					// if dragged item has class menu-item and dropped div has class drop-container, add module 
					if (dragEl.hasClass('menu-item') && dropEl.hasClass('drop-container')) {
						console.log("Drag event on " + dragIndex);
						var x = event.pageX - scope.module_css.width / 2;
						var y = event.pageY - scope.module_css.height / 2;

						scope.addModuleToSchema(dragIndex, x, y);
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