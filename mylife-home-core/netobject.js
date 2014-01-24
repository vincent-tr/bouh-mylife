var assert = require('assert');
var events = require('events');
var util = require('util');

var netstructure = require('./netstructure.js');

function netObject(id, clazz) {
	
	var obj = Object.create(events.EventEmitter);
	obj.id = id;
	obj.clazz = clazz;
	
	var attributeValues = {};
	
	for(var i=0, l=this.clazz.members.length; i<l; i++) {
		var member = this.clazz.members[i];
		if(member.membertype !== 'attribute') {
			continue;
		}
		
		attributeValues[member.name] = null;
	}
	
	obj.getMember = function(name) {
		for(var i=0, l=this.clazz.members.length; i<l; i++) {
			var member = this.clazz.members[i];
			if(member.name === name) {
				return member;
			}
		}
		
		return null;
	};

	var checkArg = function(arg, type) {
		if(type.type === 'range') {
			var val = parseInt(arg, 10);
			if(isNaN(val) || val < type.min || val > type.max) {
				throw new Error('invalid argument');
			}
		}

		if(type.type === 'enum') {
			for(var i=0, l=type.values.length; i<l; i++) {
				if(type.values[i] === arg) {
					return;
				}
			}
			
			throw new Error('invalid value');
		}
	};

	obj.executeAction = function(name) {
		
		var member = this.getMember(name);
		assert(member);
		assert(member.membertype === 'action');
		
		var args = Array.prototype.slice.call(arguments, 1);
		if(args.length !== member.args.length) {
			throw new Error('invalid arguments count');
		}
		for(var i=0, l=args.length; i<l; i++) {
			checkArg(args[i], member.args[i]);
		}
		
		obj.emit('action#' + member.name, args);
		obj.emit('action', member.name, args);
	};

	obj.setAttribute = function(name, value) {

		var member = this.getMember(name);
		assert(member);
		assert(member.membertype === 'attribute');
		
		checkArg(value, member.arg);

		attributeValues[member.name] = value;
		
		obj.emit('attribute#' + member.name, value);
		obj.emit('attribute', member.name, value);
	};

	obj.getAttribute = function(name) {
		
		var member = this.getMember(name);
		assert(member);
		assert(member.membertype === 'attribute');
		
		return attributeValues[member.name];
	};
	
	return obj;
}

module.exports.netObject = netObject;
