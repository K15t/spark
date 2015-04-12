(function() {
    // Save a copy of the existing angular for later restoration
    window.existingWindowDotAngular = window.angular;

    // create a new window.angular and a closure variable for
    // angular.js to load itself into
    var angular = (window.angular = {});

})();