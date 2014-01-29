var netobject = require('./netobject.js');
//var config = require('./config.json');

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
	
	var eventHandler = function() {
		var args = Array.prototype.slice.call(arguments, 0);
		args.unshift(config.destinationAction);
		destinationComponent.executeAction.apply(destinationComponent, args);
	};
	
	sourceComponent.on(eventName, eventHandler);
	
	var link = {
		id: id,
		destroy: function() {
			sourceComponent.removeListener(eventName, eventHandler);
		}
	};
	
	links[link.id] = link;
	return link;
};

var destroy = function(config) {
	var id = makeId(config);
	var link = links[id];
	if(link === undefined) {
		return false;
	}
	
	link.destroy();
	delete links[id];
	return true;
};

module.exports.create = create;
module.exports.destroy = destroy;
