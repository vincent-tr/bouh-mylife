var assert = require('assert');
var events = require('events');
var util = require('util');

var netstructure = require('./netstructure.js');

function NetObject(id, clazz) {
	events.EventEmitter.call(this);
	
	this.id = id;
	this.clazz = clazz;
	
	this.attributeValues = {};
	for(var i=0, l=this.clazz.members.length; i<l; i++) {
		var member = this.clazz.members[i];
		if(!(member instanceof netstructure.NetAttribute)) {
			continue;
		}
		
		this.attributeValues[member.name] = null;
	}
}

util.inherits(NetObject, events.EventEmitter);

NetObject.prototype.getMember = function(name) {
	for(var i=0, l=this.clazz.members.length; i<l; i++) {
		var member = this.clazz.members[i];
		if(member.name === name) {
			return member;
		}
	}
	
	return null;
};

var checkArg = function(arg, type) {
	if(type instanceof netstructure.NetRange) {
		var val = parseInt(arg, 10);
		if(isNaN(val) || val < type.min || val > type.max) {
			throw new Error('invalid argument');
		}
	}

	if(type instanceof netstructure.NetEnum) {
		for(var i=0, l=type.values.length; i<l; i++) {
			if(type.values[i] === arg) {
				return;
			}
		}
		
		throw new Error('invalid value');
	}
};

NetObject.prototype.executeAction = function(name) {
	
	var member = this.getMember(name);
	assert(member);
	assert(member instanceof netstructure.NetAction);
	
	var args = Array.prototype.slice.call(arguments, 1);
	if(args.length !== member.args.length) {
		throw new Error('invalid arguments count');
	}
	for(var i=0, l=args.length; i<l; i++) {
		checkArg(args[i], member.args[i]);
	}
	
    this.emit('action#' + member.name, args);
    this.emit('action', member.name, args);
};

NetObject.prototype.setAttribute = function(name, value) {

	var member = this.getMember(name);
	assert(member);
	assert(member instanceof netstructure.NetAttribute);
	
	checkArg(value, member.arg);

	this.attributeValues[member.name] = value;
	
	this.emit('attribute#' + member.name, value);
    this.emit('attribute', member.name, value);
};

NetObject.prototype.getAttribute = function(name) {
	
	var member = this.getMember(name);
	assert(member);
	assert(member instanceof netstructure.NetAttribute);
	
	return this.attributeValues[member.name];
};

module.exports.NetObject = NetObject;
