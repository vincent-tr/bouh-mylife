var netobject = require('./netobject.js');

var links = {};

var makeId = function(config) {
	return config.sourceComponent + '|' + config.sourceAttribute + '|' + config.destinationComponent + '|' + config.destinationAction;
};

var create = function(config) {
	
	var id = makeId(config);
	if(links[id] !== undefined) {
		throw new Error('link already exists');
	}
	
	var sourceComponent = netobject.netRepository[config.sourceComponent].object;
	var destinationComponent = netobject.netRepository[config.destinationComponent].object;
	
	var eventName = 'attribute#' + config.sourceAttribute;
	
	var eventHandler = function(value) {
		if(value === undefined) {
			value = sourceComponent.getAttribute(config.sourceAttribute);
		}
		destinationComponent.executeAction.apply(destinationComponent, [config.destinationAction, value]);
	};
	
	sourceComponent.on(eventName, eventHandler);
	sourceComponent.on('connected', eventHandler);
	
	var link = {
		id: id,
		config: config,
		destroy: function() {
			sourceComponent.removeListener(eventName, eventHandler);
		}
	};
	
	links[link.id] = link;
	return link;
};

var destroy = function(id) {
	//var id = makeId(config);
	var link = links[id];
	if(link === undefined) {
		return false;
	}
	
	link.destroy();
	delete links[id];
	return true;
};

var list = function() {
	return links;
};

module.exports.create = create;
module.exports.destroy = destroy;
module.exports.list = list;
