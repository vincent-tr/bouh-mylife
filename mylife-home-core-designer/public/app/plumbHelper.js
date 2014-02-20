/**
 * Gestion de jsPlumb
 */


'use strict';

var module = angular.module('mylife.plumbHelper', ['mylife.tools', 'mylife.schemaHelper']);

module.factory('plumbHelper', ['$timeout', '$rootScope', 'tools', 'dialogAlert', 'schemaHelper', function($timeout, $rootScope, tools, dialogAlert, schemaHelper) {
	
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
			
			link.internal().connection = connection;
			connection.link = link;
		});
	};
	
	var destroyConnection = function(link) {
		var connection = link.internal().connection;
		if(!connection)
			return;
		jsPlumb.doWhileSuspended(function() {
			jsPlumb.detach(connection);
		}, true);
		delete link.internal().connection;
	};
	
	var findConnection = function(data, 
			sourceId, targetId) {
		
		for(var i=0, l=data.links.length; i<l; i++) {
			var link = data.links[i];
			var currentSourceId = getSourceId(link);
			var currentTargetId = getTargetId(link);
			
			if(sourceId === currentSourceId && targetId === currentTargetId) {
				return link;
			}
		}
		
		return undefined;
	};
	
	var cancelConnection = function(connection) {
		jsPlumb.doWhileSuspended(function() {
			jsPlumb.detach(connection);
		}, true);
	};
	
	var createLink = function(data, sourceId, targetId) {
		
		var source = sourceId.split(':');
		var target = targetId.split(':');
		
		var link = {
			sourceComponent: source[1],
			sourceAttribute: source[2],
			destinationComponent: target[1],
			destinationAction: target[2],
		};
		
		tools.attachInternal(link);
		
		data.links.push(link);
	};
	
	var getMemberFromId = function(data, id) {
		var split = id.split(':');
		var compId = split[1];
		var memberId = split[2];
		
		return schemaHelper.getMember(data, compId, memberId);
	};
	
	var checkLinkTypes = function(data, sourceId, targetId) {
		var sourceMember = getMemberFromId(data, sourceId);
		var targetMember = getMemberFromId(data, targetId);
		
		return schemaHelper.checkLinkTypes(sourceMember, targetMember);
	};
	
	var onConnectionCreated = function(data, info, originalEvent) {
		// programmatically
		if(!originalEvent)
			return;
		
		// on annule de toutes facons pour la refaire correctement
		cancelConnection(info.connection);

		var sourceId = $(info.source).attr('schema-id');
		var targetId = $(info.target).attr('schema-id');

		// if a connection with same source/dest already exists, delete
		if(findConnection(data, sourceId, targetId)) {
			dialogAlert({text: 'Lien déjà existant !'});
			return;
		}
		
		if(!checkLinkTypes(data, sourceId, targetId)) {
			dialogAlert({text: 'Types incompatibles entre chaque extrémité du lien !'});
			return;
		}
		
		// Create
		createLink(data, sourceId, targetId);
	};
	
	var onConnectionDblClick = function(data, connection, originalEvent) {
		tools.removeFromArray(data.links, connection.link);
	};
	
	var initBindings = function(data) {
		
		jsPlumb.bind('connection', function(info, originalEvent) {
			$rootScope.$apply(function() {
				onConnectionCreated(data, info, originalEvent);
			});
		});
		
		jsPlumb.bind('dblclick', function(connection, originalEvent) {
			$rootScope.$apply(function() {
				onConnectionDblClick(data, connection, originalEvent);
			});
		});
	};
	
	return {
		makeSource: makeSource,
		makeTarget: makeTarget,
		createConnection: createConnection,
		destroyConnection: destroyConnection,
		initBindings: initBindings
	};
}]);
