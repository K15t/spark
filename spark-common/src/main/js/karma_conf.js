module.exports = function(config) {
  config.set({
      basePath: '',
      frameworks: ['jasmine'],
      reporters: ['spec'],
      files: [
          'node_modules/jquery/dist/jquery.js',
          'node_modules/soyutils/soyutils_nogoog.js',
          'test/mocks/**/*.js',
          'target/build/**/*.soy.js',
          'src/spark-bootstrap.js',
          'test/specs/**/*.js'
      ],
      singleRun: true,
      browsers: [/*'Chrome'*/ 'PhantomJS']
  });
};