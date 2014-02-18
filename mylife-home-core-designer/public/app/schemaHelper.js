/**
 * Helpers pour le schema
 */

'use strict';

var module = angular.module('mylife.schemaHelper', ['mylife.tools', 'mylife.idGenerator']);

module.factory('schemaHelper', ['tools', 'idGenerator', function(tools, toolsidGenerator) {

	var memberTitle = function(member) {
		var newLine = '\n'; // '&#10;';
		
		var formatType = function(type) {
			if(type.type === 'enum') {
				var text = '[';
				for(var i=0, l=type.values.length; i<l; i++) {
					if(i > 0) {
						text += ',';
					}
					text += type.values[i];
				}
				return text + ']';
			} else {
				return '(' + type.min + ';' + type.max + ')';
			}
		};
		
		var formatAttribute = function(member) {
			var text = 'Attribut: ' + member.name;
			text += newLine + 'Type: ' + formatType(member.type);
			return text;
		};
		
		var formatAction = function(member) {
			var text = 'Action: ' + member.name;
			for(var i=0, l=member.arguments.length; i<l; i++) {
				text += newLine + 'Argument #' + i + ': ' + formatType(member.arguments[i]);
			}
			return text;
		};
		
		if(member.membertype === 'attribute') {
			return formatAttribute(member);
		} else {
			return formatAction(member);
		}
	};
	
	var toolboxTitle = function(pluginType) {
		var text = pluginType.displayName;
		if(pluginType.ui)
			text += ' (ui)';
		return text;
	};
	
	var findType = function(data, typeId) {
		var pluginTypes = data.pluginTypes;
		for(var i=0, l=pluginTypes.length; i<l; i++) {
			var type = pluginTypes[i];
			if(type.id === typeId)
				return type;
		}
		return undefined;
	};
	
	var createPlugin = function(data, typeId, x, y) {
		
		var type = findType(data, typeId);
		
		var parameters = {};
		for(var i=0, l=type.arguments.length; i<l; i++) {
			parameters[type.arguments[i]] = null;
		}
		
		var plugin = {
			id: 'new_plugin_' + idGenerator(),
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
		
		data.plugins.push(plugin);
	};
	
	var findLinksFromComponent = function(data, comp) {
		var links = [];
		for(var i=0, l=data.links.length; i<l; i++) {
			var link = data.links[i];
			if(link.sourceComponent === comp.id || link.destinationComponent === comp.id) {
				links.push(link);
			}
		}
		return links;
	};
	
	var deleteComponent = function(data, comp) {
		
		// suppression des liens
		var links = findLinksFromComponent(data, comp);
		links.forEach(function(link) {
			tools.removeFromArray(data.links, link);
		});
		
		// suppression du composant
		var isPlugin = comp.internal.type ? true : false;
		if(isPlugin) {
			tools.removeFromArray(data.plugins, comp);
		} else {
			tools.removeFromArray(data.hardware, comp);
		}
	};

	return {
		memberTitle: memberTitle,
		toolboxTitle: toolboxTitle,
		createPlugin: createPlugin,
		deleteComponent: deleteComponent
	};
}]);