window.SPARK = window.SPARK || {};

/*
 * This script is meant to be loaded before soy-templates file and the main spark-bootstrap file.
 * A SPARK.__versions helper object is initialized to the SPARK global object (if it did not exists).
 * When spark-bootstrap script finishes, it will add the initialized SPARK instance (version) to the
 * __versions helper calling __versions.add() that will store a reference to the new SPARK
 * version and restore the old SPARK global object (the __versions property is added to that version if needed).
 * A reference to a SPARK version can be fetched by calling SPARK.__versions.get()
 * Calling __versions.get() returns the latest added version, for added version-security the method can
 * also be called with __versions.get(<version_string>). Then the (first) SPARK version containing
 * a property '__version' that equals the <version_string> will be returned.
 */

// store old version and clear SPARK namespace for the new version
var oldSpark = window.SPARK;
window.SPARK = {};

var initVersions = function() {

    var versionStore = [];

    var oldSparkRef = {};

    // the __versions helper can be shared between multiple executions of noconflict-header and the oldSpark
    // stored in the outer function scope is not necessarily the latest SPARK global object
    // (though if that would happen something would very likely be wrong, as every script should only add
    // to the global object or restore it to original state after running)...
    var storeOld = function(oldSpark) {
        oldSparkRef = oldSpark;
    };

    var add = function(newVersion) {
        // prepend the new version
        versionStore.unshift(newVersion);
        window.SPARK = oldSparkRef;
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
        'get': get,
        'storeOld': storeOld
    }
};

// initialize the __versions-helper if needed
// make sure that it stays in the global SPARK-object and is shared between SPARK-versions
var versions;
if (oldSpark) {
    if (oldSpark.__versions) {
        versions = oldSpark.__versions;
    } else {
        versions = initVersions();
        // this must stay available in the SPARK global object also after replacing the
        // new SPARK global object with the old one
        oldSpark.__versions = versions;
    }
} else {
    versions = initVersions();
}

versions.storeOld(oldSpark);

window.SPARK.__versions = versions;
