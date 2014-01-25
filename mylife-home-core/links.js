var netobject = require('./netobject.js');
var config = require('./config.json');

var links = [];

var create = function(config) {
	
	var sourceComponent = netobject.netRepository[config.sourceComponent].object;
	var destinationComponent = netobject.netRepository[config.destinationComponent].object;
	
	var eventName = 'attribute#' + config.sourceAttribute;
	
	var eventHandler = function() {
		var args = Array.prototype.slice.call(arguments, 0);
		args.unshift(config.destinationAction);
		destinationComponent.executeAction.apply(destinationComponent, args);
	};
	
	sourceComponent.on(eventName, eventHandler);
	
	return {
		destroy: function() {
			sourceComponent.removeListener(eventName, eventHandler);
		}
	};
};

var initialize = function() {
	var linksConfig = config.links;
	for (var i = 0, l = linksConfig.length; i < l; i++) {
		var linkConfig = linksConfig[i];
		var link = create(linkConfig);
		links.push(link);
	}
};

var terminate = function() {
	for (var i = 0, l = links.length; i < l; i++) {
		var link = links;
		link.destroy();
	}
	links.length = 0;
};

module.exports.initialize = initialize;
module.exports.terminate = terminate;
