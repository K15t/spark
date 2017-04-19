import spark from './index';

function getSparkFail() {
    throw new Error('getSpark() has to be called immediately after including spark-dist.js!');
}

/**
 * Get the last included version of Spark. Has to be called immediately after including spark-dist.js (to avoid another version of SPARK
 * to be included in the meantime).
 * @param allowMultipleCalls {Boolean} Allow getSpark() to be called once more (useful to set if you have multiple JS files that are
 *                                     included directly after each other and all want to access SPARK.
 */
function getSpark(allowMultipleCalls) {
    if (!allowMultipleCalls) {
        window.getSpark = getSparkFail;
    }

    return spark;
}

window.getSpark = getSpark;

setTimeout(function() {
    if (window.getSpark === getSpark) {
        window.getSpark = getSparkFail;
    }
}, 0);
