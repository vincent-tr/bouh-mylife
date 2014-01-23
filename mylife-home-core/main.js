var assert = require('assert');
var events = require('events');
var util = require('util');

var netstructure = require('./netstructure.js');
var netobject = require('./netobject.js');
var netrepository = require('./netrepository.js');

var onoff = new netstructure.NetEnum("off", "on");
var clazz = new netstructure.NetClass(
		new netstructure.NetAttribute("value", onoff),
		new netstructure.NetAction("setvalue", onoff));

var obj = new netobject.NetObject("node_test", clazz);
obj.setAttribute("value", "off");

var setvalue = function(args) {
	obj.setAttribute("value", args[0]);
};

obj.on("action#setvalue", setvalue);

var container = netrepository.publish(obj, 'mylife-home-hardware', true);