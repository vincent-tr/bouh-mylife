/**
 * Gestion de l'application
 */

// https://github.com/mrquincle/jsplumb-example

'use strict';

var app = angular.module('mylife.app', ['mylife.api'/*, 'ui.bootstrap'*/]);

app.factory('plumbHelper', ['$timeout', function($timeout) {
	
	var epPaintStyle = {
		strokeStyle:"#7AB02C",
		fillStyle:"transparent",
		radius:4,
		lineWidth:3 
	};
	
	var epHoverPaintStyle = {
		strokeStyle:"#7AB02C",
		fillStyle:"darkgray",
		radius:4,
		lineWidth:3 
	};
	
	var container = $('#schema-container');
	
	var makeSource = function(element) {
		jsPlumb.makeSource(element, {
			container: container,
			deleteEndpointsOnDetach: true,
			anchor:[ ["Left"], ["Right"] ],
			maxConnections: -1,
			paintStyle: epPaintStyle,
			hoverPaintStyle: epHoverPaintStyle,
			
			connector: [ "Flowchart", { stub:[40, 60], gap:3, cornerRadius:5, alwaysRespectStubs:true } ],
			connectorStyle: {
				lineWidth:4,
				strokeStyle:"lightgray",
			},
			connectorHoverStyle: {
				lineWidth:4,
				strokeStyle:"darkgray",
			},
			connectorOverlays: [ [ "Arrow", { location:1, width: 10, length: 10 } ] ]
		});
	};
	
	var makeTarget = function(element) {
		jsPlumb.makeTarget(element, {
			container: container,
			deleteEndpointsOnDetach: true,
			anchor:[ ["Left"], ["Right"] ],
			maxConnections: -1,
			paintStyle: epPaintStyle,
			hoverPaintStyle: epHoverPaintStyle,
		});
	};
	
	/**********************************/
	
	var getSourceId = function(link) {
		return 'attribute:' + link.sourceComponent + ':' + link.sourceAttribute;
	};
	
	var getTargetId = function(link) {
		return 'action:' + link.destinationComponent + ':' + link.destinationAction;;
	};
	
	var createConnection = function(link) {
		var sourceId = getSourceId(link);
		var targetId = getTargetId(link);
		
		$timeout(function(){
			var source = $("div[schema-id='" + sourceId +"']");
			var target = $("div[schema-id='" + targetId +"']");
			var connection = jsPlumb.connect({source : source, target : target });
			
			link.internal.connection = connection;
			connection.link = link;
		});
	};
	
	var destroyConnection = function(link) {
		var connection = link.internal.connection;
		if(!connection)
			return;
		jsPlumb.doWhileSuspended(function() {
			jsPlumb.detach(connection);
		}, true);
		delete link.internal.connection;
	};
	
	var destroyConnectionFromItem = function(plugin) {
		// TODO
	};
	
	var onConnectionCreated = function(info, originalEvent) {
		// programmatically
		if(!originalEvent)
			return;
		
		// if a connection with same source/dest already exists, delete
		alert('onConnectionCreated');
	};
	
	var onConnectionDblClick = function(connection, originalEvent) {
		alert('onConnectionDblClick');
	};
	
	return {
		makeSource: makeSource,
		makeTarget: makeTarget,
		createConnection: createConnection,
		destroyConnection: destroyConnection,
		destroyConnectionFromItem: destroyConnectionFromItem,
		onConnectionCreated: onConnectionCreated,
		onConnectionDblClick: onConnectionDblClick
	};
}]);

app.controller('designerController', ['$scope', '$timeout', 'api', 'plumbHelper', function($scope, $timeout, api, plumbHelper) {

	$scope.pluginTypes = [];
	$scope.plugins = [];
	$scope.hardware = [];
	$scope.links = [];
	$scope.selectedComponent = null;
	$scope.id_generator = 0;
	
	var newId = function() {
		return ++($scope.id_generator);
	};

	var findType = function(typeId) {
		var pluginTypes = $scope.pluginTypes;
		for(var i=0, l=pluginTypes.length; i<l; i++) {
			var type = pluginTypes[i];
			if(type.id === typeId)
				return type;
		}
		return undefined;
	};
	
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

		var attachInternal = function(item) {
			if(!item.internal) {
				item.internal = {};
			}
		};
		
		var attachTypeToPlugin = function(plugin) {
			
			for (var i = 0, l = $scope.pluginTypes.length; i < l; i++) {
				var type = $scope.pluginTypes[i];
				if (type.id === plugin.type) {
					plugin.internal.type = type;
					break;
				}
			}
		};
		
		$scope.pluginTypes = [];
		$scope.plugins = [];
		$scope.hardware = [];
		$scope.links = [];
		$scope.selectedComponent = null;
	
		$scope.pluginTypes = data.pluginTypes;
		$scope.plugins = data.plugins;
		$scope.hardware = data.hardware;
		$scope.links = data.links;

		$scope.pluginTypes.forEach(attachInternal);
		$scope.plugins.forEach(attachInternal);
		$scope.hardware.forEach(attachInternal);
		$scope.links.forEach(attachInternal);
		
		$scope.plugins.forEach(attachTypeToPlugin);
		
		$scope.plugins.forEach(checkDesignerData);
		$scope.hardware.forEach(checkDesignerData);
	};
	
	$scope.reload = function() {
		api.data.get({}, function(data) {
			applyData(data);
		});
	};
	
	$scope.updateHardware = function(url) {
		// on envoie l'url et on récupère une version du schéma avec le nouveau matériel dessus (non sauvegardé)
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
		
		var type = findType(typeId);
		
		var parameters = {};
		for(var i=0, l=type.arguments.length; i<l; i++) {
			parameters[type.arguments[i]] = null;
		}
		
		var plugin = {
			id: 'new_plugin_' + newId(),
			type: type.id,
			parameters: parameters,
			designer: {
				x: x,
				y: y
			},
			internal: {
				type: type
			}
		};
		
		$scope.plugins.push(plugin);
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
	
	var plumbBind = function() {
		jsPlumb.bind('connection', function(info, originalEvent) {
			$scope.$apply(function() {
				plumbHelper.onConnectionCreated(info, originalEvent);
			});
		});
		
		jsPlumb.bind('dblclick', function(connection, originalEvent) {
			$scope.$apply(function() {
				plumbHelper.onConnectionDblClick(connection, originalEvent);
			});
		});
	};
	
	$scope.init = function() {
		plumbBind();
		$scope.reload();
	};
}]);

app.directive('initializer', [ '$timeout', function($timeout) {
	return {
		restrict : 'A', 
		terminal : true,
		transclude : true,
		link : function(scope, element, attrs) {
			$timeout(scope.init, 0);
		}
	};
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
			
			$(element).draggable({
				revert: true,
				helper: 'clone',
				containment: $('#main'),
				zIndex: 100
			});
		}
	};
});

//directives link user interactions with $scope behaviours
//now we extend html with <div plumb-item>, we can define a template <> to replace it with "proper" html, or we can 
//replace it with something more sophisticated, e.g. setting jsPlumb arguments and attach it to a double-click 
//event
app.directive('schemaItem', ['plumbHelper', function(plumbHelper) {
	return {
		replace: true,
		controller: 'designerController',
		link: function (scope, element, attrs) {

			jsPlumb.draggable(element, {
				containment: 'parent'
			});
			
			element.bind('click', function() {
				$(element).addClass('item-selected').siblings().removeClass('item-selected');
				
				// Soit un plugin soit un hwitem (dans les ng-repeat)
				var component = scope.plugin;
				if(!component)
					component = scope.hwitem;
				
				// Scope parent du ng-repeat
				scope.$parent.selectedComponent = component;
			});
/*
			scope.$on('$destroy', function() {
				plumbHelper.destroyConnectionFromItem(scope.plugin);
			});
*/			
/*
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
*/
		}
	};
}]);

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

app.directive('schemaLink', ['plumbHelper', function(plumbHelper) {
	return {
		replace: true,
		controller: 'designerController',
		link: function(scope, element, attrs){
			plumbHelper.createConnection(scope.link);
			
			scope.$on('$destroy', function() {
				plumbHelper.destroyConnection(scope.link);
			});
		}
	};
}]);

app.directive('schemaContainer', function($compile) {
	return {
		restrict: 'A',
		link: function(scope, element, attrs){

			element.droppable({
				drop: function(event, ui) {
					scope.$apply(function() {
						
						var typeId = angular.element(ui.draggable).data('identifier'),
						dragElement = angular.element(ui.draggable),
						dropElement = element;
	
						// if dragged item has class menu-item and dropped div has class drop-container, add module 
						if (dragElement.hasClass('toolbox-item') && dropElement.hasClass('drop-container')) {
							var x = event.pageX - dropElement.offset().left;
							var y = event.pageY - dropElement.offset().top;
	
							scope.createPlugin(typeId, x, y);
						}
					});
				}
			});
		}
	};
});

app.directive('splitter', ['$timeout', function($timeout) {
	return {
		restrict:'A',
		link: function(scope, element, attrs) {
			
			var verticalSplitter = function() {
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
					minWidth: '110',
					maxWidth: '500',
					resize: resizeRight
				});
				
				$timeout(function(){
					resizeRight();
				});
			};
			
			var horizontalSplitter = function() {
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
					minHeight: '100',
					resize: resizeBottom
				});
				
				$timeout(function(){
					resizeBottom();
				});
			};
			
			var direction = attrs.splitter;
			if(direction !== 'horizontal' && direction !== 'vertical')
				direction = 'vertical';
			
			if(direction === 'vertical') {
				verticalSplitter();
			} else {
				horizontalSplitter();
			}
		}
	};
}]);
