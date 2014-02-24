/**
 * Helpers pour le schema
 */

'use strict';

var module = angular.module('mylife.component.schemaHelper', ['mylife.tools', 'mylife.idGenerator']);

module.factory('schemaHelper', ['tools', 'idGenerator', function(tools, idGenerator) {

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
			}
		};
		
		tools.attachInternal(plugin);
		plugin.internal().type = type;
		
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
		var isPlugin = comp.internal().type ? true : false;
		if(isPlugin) {
			tools.removeFromArray(data.plugins, comp);
		} else {
			tools.removeFromArray(data.hardware, comp);
		}
	};

	var typeEquals = function(type1, type2) {
		if(type1.type !== type2.type) {
			return false;
		}
		
		switch(type1.type) {
		case 'range':
			return type1.min === type2.min && type1.max === type2.max;
			
		case 'enum':
			if(type1.values.length !== type2.values.length) {
				return false;
			}
			
			for(var i=0, l=type1.values.length; i<l; i++) {
				if(type1.values[i] !== type2.values[i]) {
					return false;
				}
			}
			return true;
			
		default:
			return false;
		}
	};
	
	var getMember = function(data, compId, memberId) {
		
		var clazz = undefined;
		
		// recherche hardware
		if(!clazz) {
			for(var i=0, l=data.hardware.length; i<l; i++) {
				var hwitem = data.hardware[i];
				if(hwitem.id == compId) {
					clazz = hwitem['class'];
					break;
				}
			}
		}
		
		// recherche plugin
		if(!clazz) {
			for(var i=0, l=data.plugins.length; i<l; i++) {
				var plugin = data.plugins[i];
				if(plugin.id == compId) {
					clazz = plugin.internal().type['class'];
					break;
				}
			}
		}
		
		if(!clazz) {
			return undefined;
		}
		
		for(var i=0, l=clazz.members.length; i<l; i++) {
			var member = clazz.members[i];
			if(member.name === memberId) {
				return member;
			}
		}
		
		return undefined;
	};

	var checkLinkTypes = function(sourceMember, targetMember) {
		if(targetMember.arguments.length === 0)
			return true;
		if(targetMember.arguments.length > 1)
			return false;
		return typeEquals(targetMember.arguments[0], sourceMember.type);
	};
	
	return {
		memberTitle: memberTitle,
		toolboxTitle: toolboxTitle,
		createPlugin: createPlugin,
		deleteComponent: deleteComponent,
		findLinksFromComponent: findLinksFromComponent,
		typeEquals: typeEquals,
		getMember: getMember,
		checkLinkTypes: checkLinkTypes
	};
}]);