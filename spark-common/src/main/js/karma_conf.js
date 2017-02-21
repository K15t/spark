module.exports = function(config) {
  config.set({
      basePath: '',
      frameworks: ['jasmine'],
      reporters: ['spec'],
      files: [
          'test/mocks/**/*.js',
          'src/spark-bootstrap.js',
          'test/specs/**/*.js'
      ],
      singleRun: true,
      browsers: [/*'Chrome'*/, 'PhantomJS']
  });
};