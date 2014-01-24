
function netRange(min, max) {
	return {
		type: "range",
		min: min,
		max: max
	};
}

function netEnum() {
	return {
		type: "enum",
		values: Array.prototype.slice.call(arguments, 0)
	};
}

function netAttribute(name, type) {
	return {
		name: name,
        membertype: "attribute",
        type: type
	};
}

function netAction(name) {
	return {
		name: name,
		membertype: "action",
		arguments: Array.prototype.slice.call(arguments, 1)
	};
}

function netClass() {
	return {
		members: Array.prototype.slice.call(arguments, 0)
	};
}

module.exports.netRange = netRange;
module.exports.netEnum = netEnum;
module.exports.netAttribute = netAttribute;
module.exports.netAction = netAction;
module.exports.netClass = netClass;
