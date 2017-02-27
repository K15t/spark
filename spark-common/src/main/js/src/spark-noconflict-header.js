var SPARK = SPARK || {};

(function() {

    // store old version and clear SPARK namespace for the new version
    var oldSpark = SPARK;
    SPARK = {};

    var initVersions = function() {

        var versionStore = [];

        var add = function(newVersion) {
            // prepend the new version
            versionStore.unshift(newVersion);
            SPARK = oldSpark;
        };

        var get = function(versionToGet) {
            if (versionToGet) {
                for (var i = 0; i < versionStore.length; i++) {
                    if (versionStore[i].__version === versionToGet) {
                        return versionStore[i];
                    }
                }
                return undefined;
            } else {
                // if no version specified, return latest added version
                return versionStore[0];
            }
        };

        return {
            'add': add,
            'get': get
        }
    };

    var versions;
    if (oldSpark) {
        if (oldSpark.__versions) {
            versions = oldSpark.__versions;
        } else {
            versions = initVersions();
            // this must stay available in the SPARK global object
            oldSpark.__versions = versions;
        }
    } else {
        versions = initVersions();
    }

    // init __versions somehow

    SPARK.__versions = versions;

})();