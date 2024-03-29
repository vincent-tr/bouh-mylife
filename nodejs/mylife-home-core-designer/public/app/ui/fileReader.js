/**
 * Lecture de fichier
 * http://odetocode.com/blogs/scott/archive/2013/07/03/building-a-filereader-service-for-angularjs-the-service.aspx
 */

'use strict';

var module = angular.module('mylife.ui.fileReader', []);

module.factory('fileReader', ["$q", function($q) {
	
	var onLoad = function(reader, deferred, scope) {
        return function () {
            scope.$apply(function () {
                deferred.resolve(reader.result);
            });
        };
    };

    var onError = function (reader, deferred, scope) {
        return function () {
            scope.$apply(function () {
                deferred.reject(reader.result);
            });
        };
    };
/*
    var onProgress = function(reader, scope) {
        return function (event) {
            scope.$broadcast("fileProgress",
                {
                    total: event.total,
                    loaded: event.loaded
                });
        };
    };
*/
    var getReader = function(deferred, scope) {
        var reader = new FileReader();
        reader.onload = onLoad(reader, deferred, scope);
        reader.onerror = onError(reader, deferred, scope);
        //reader.onprogress = onProgress(reader, scope);
        return reader;
    };

    var readAsDataURL = function (file, scope) {
        var deferred = $q.defer();
         
        var reader = getReader(deferred, scope);         
        reader.readAsDataURL(file);
         
        return deferred.promise;
    };

    return {
        readAsDataUrl: readAsDataURL  
    };
}]);

module.directive('inputData', ['$parse', 'fileReader', function($parse, fileReader) {
	return {
		link: function (scope, element, attrs) {
			var expr = attrs.inputData;
			var getter = $parse(expr);
			var setter = getter.assign;
			
			element.on('change', function() {
				var files = element[0].files;
				if(files.length === 0) {
					scope.$apply(function () {
						setter(scope, '');
					});
					return;
				}
				
				fileReader.readAsDataUrl(files[0], scope).then(function(url) {
					setter(scope, url);
				});
			});
		}
	};
}]);
