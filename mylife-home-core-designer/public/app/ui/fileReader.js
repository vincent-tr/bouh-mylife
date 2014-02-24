/**
 * Lecture de fichier
 * http://odetocode.com/blogs/scott/archive/2013/07/03/building-a-filereader-service-for-angularjs-the-service.aspx
 */

'use strict';

var module = angular.module('mylife.ui.fileReader');

module.factory("fileReader", ["$q", "$log", function($q, $log) {
	
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

// TODO : directive pour input file pour attacher le onchange à un élément du scope
//module.directive()