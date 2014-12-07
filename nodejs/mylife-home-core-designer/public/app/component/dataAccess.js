/**
 * Accès aux données
 */


'use strict';

var module = angular.module('mylife.component.dataAccess', ['mylife.api', 'mylife.tools', 'ui.bootstrap', 'mylife.component.schemaHelper']);

module.factory('componentDataAccess', ['$modal', 'api', 'tools', 'schemaHelper', function($modal, api, tools, schemaHelper) {
	
	var checkDesignerData = function(item) {
		var designer = item.designer;
		if(!designer) {
			item.designer = designer = {
				x: 0,
				y: 0
			};
		}
	};

	var prepareData = function(data) {
		
		var attachTypeToPlugin = function(plugin) {
			
			for (var i = 0, l = data.pluginTypes.length; i < l; i++) {
				var type = data.pluginTypes[i];
				if (type.id === plugin.type) {
					plugin.internal().type = type;
					break;
				}
			}
		};
	
		data.pluginTypes.forEach(tools.attachInternal);
		data.plugins.forEach(tools.attachInternal);
		data.hardware.forEach(tools.attachInternal);
		data.links.forEach(tools.attachInternal);
		
		data.plugins.forEach(attachTypeToPlugin);
		
		data.plugins.forEach(checkDesignerData);
		data.hardware.forEach(checkDesignerData);
	};
	
	var load = function(callback) {
		api.component.data.get({}, function(data) {
			prepareData(data);
			if(callback) {
				callback(data);
			}
		});
	};
	
	var save = function(data, callback) {
		
		var prepareArray = function(source) {
			return tools.clone(source);
		};
		
		var sendData = {
			plugins: prepareArray(data.plugins),
			hardware: prepareArray(data.hardware),
			links: prepareArray(data.links)
		};
		
		api.component.merge.post({}, sendData, function(mergeData) {
			
			var modalInstance = $modal.open({
				templateUrl: 'mergeConfirm.html',
				controller: function ($scope, $modalInstance) {

					$scope.destroy = mergeData.destroy;
					$scope.create = mergeData.create;
					
					$scope.ok = function () {
						$modalInstance.close();
					};

					$scope.cancel = function () {
						$modalInstance.dismiss();
					};
				}
			});

			modalInstance.result.then(function () {
				api.component.apply.post({}, mergeData, function() {
					callback();
				});
			});
		});
	};
	
	var loadHardware = function(data, url) {
		
		var findItem = function(id) {
			for(var i=0, l=data.hardware.length; i<l; i++) {
				var hwitem = data.hardware[i];
				if(hwitem.id === id) {
					return hwitem;
				}
			}
			return undefined;
		};
		
		var sendData = {
			url: url
		};
		
		api.component.hardware.post({}, sendData, function(res) {
			
			var hwitems = res.components;
			var failedLinks = [];
			
			// check si l'item existe déjà,
			// s'il n'existe pas ajout, 
			// sinon sauvegarde des links, suppression, ajout, et recreation des links si possible (si les membres existent encore et sont du bon type)
			
			hwitems.forEach(function(item) {
				
				var existingItem = findItem(item.id);
				var recreateLinks = [];
				
				if(existingItem) {
					recreateLinks = schemaHelper.findLinksFromComponent(data, existingItem);
					schemaHelper.deleteComponent(data, existingItem);
				}
				
				tools.attachInternal(item);

				if(existingItem) {
					// copie des infos de design
					item.designer = existingItem.designer;
				}
				
				checkDesignerData(item);
				data.hardware.push(item);
				
				recreateLinks.forEach(function(link) {
					
					var sourceMember = schemaHelper.getMember(data, link.sourceComponent, link.sourceAttribute);
					var destMember = schemaHelper.getMember(data, link.destinationComponent, link.destinationAction);
					
					if(!sourceMember || !destMember) {
						failedLinks.push({link: link, reason: 'Le membre n\'existe plus'});
						return;
					}
					
					if(!schemaHelper.checkLinkTypes(sourceMember, destMember)) {
						failedLinks.push({link: link, reason: 'Les types ne correspondent plus'});
						return;
					}
					
					var newLink = {
						sourceComponent: link.sourceComponent,
						sourceAttribute: link.sourceAttribute,
						destinationComponent: link.destinationComponent,
						destinationAction: link.destinationAction,
						id: link.id
					};
					tools.attachInternal(newLink);
					data.links.push(newLink);
				});
			});
			
			// TODO : afficher failedLinks
		});
	};
	
	return {
		load: load,
		save: save,
		loadHardware: loadHardware
	};
	
}]);
