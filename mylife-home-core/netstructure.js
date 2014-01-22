var assert = require('assert');
var util = require('util');

function NetType() {
}

function NetEnum() {
	NetType.call(this);
	this.values = Array.prototype.slice.call(arguments, 0);
}

util.inherits(NetEnum, NetType);

function NetRange(min, max) {
	NetType.call(this);
	this.min = min;
	this.max = max;
}

util.inherits(NetRange, NetType);

function NetMember(name) {
	this.name = name;
}

function NetAction(name) {
	NetMember.call(this, name);
	
	this.args = Array.prototype.slice.call(arguments, 1);
	
	for(var i=0, l=this.args.length; i<l; i++) {
		assert(this.args[i] instanceof NetType);
	}
}

util.inherits(NetAction, NetMember);

function NetAttribute(name, type) {
	NetMember.call(this, name);
	this.arg = type;
	assert(this.arg instanceof NetType);
}

util.inherits(NetAttribute, NetMember);

function NetClass() {
	this.members = Array.prototype.slice.call(arguments, 1);
	
	for(var i=0, l=this.members; i<l; i++) {
		assert(this.members[i] instanceof NetMember);
	}
}

module.exports.NetType = NetType;
module.exports.NetEnum = NetEnum;
module.exports.NetRange = NetRange;
module.exports.NetMember = NetMember;
module.exports.NetAction = NetAction;
module.exports.NetAttribute = NetAttribute;
module.exports.NetClass = NetClass;
