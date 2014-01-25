var assert = require('assert');
var events = require('events');
var util = require('util');

var plugins = require('./plugins.js');
var links = require('./links.js');

plugins.initialize();
links.initialize();
