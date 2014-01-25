var assert = require('assert');
var events = require('events');
var util = require('util');

//var netobject = require('./netobject.js');
/*
var onoff = netobject.netEnum("off", "on");
var clazz = netobject.netClass(
		netobject.netAttribute("value", onoff),
		netobject.netAction("setvalue", onoff));

var obj = netobject.netObject("node_test", clazz);
obj.setAttribute("value", "off");

var setvalue = function(args) {
	obj.setAttribute("value", args[0]);
};

obj.on("action#setvalue", setvalue);

var container = netobject.publish(obj, 'mylife-home-hardware', true);
*/

var plugins = require('./plugins.js');

plugins.initialize();