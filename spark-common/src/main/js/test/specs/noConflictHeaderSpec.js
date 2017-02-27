describe('No conflict version handling', function() {

   it('adds the __versions controller', function() {

        expect(SPARK.__versions).toEqual(jasmine.any(Object));

        expect(SPARK.__versions.get).toEqual(jasmine.any(Function));

        expect(SPARK.__versions.add).toEqual(jasmine.any(Function));

   });

   it('get returns (latest) SPARK', function() {

      var getRes = SPARK.__versions.get();

      expect(getRes).toEqual(jasmine.any(Object));

      expect(getRes.appLoader2).toBeDefined();
      expect(getRes.iframeAppLoader).toBeDefined();

   });

   it('restores old SPARK global object after loading new', function() {

       expect(SPARK).toEqual(AJS.testControl.oldSparkMockupVersion);

       expect(SPARK.mockupOldVersion).toEqual(true);

       expect(SPARK.appLoader2).not.toBeDefined();
       expect(SPARK.iframeAppLoader).not.toBeDefined();

   });

   it('returns correct version by version string', function() {

       var latestRealSpark = SPARK.__versions.get();

       var fakeSpark = {'__version': '0.0.1'};

       SPARK.__versions.add(fakeSpark);

       var latestFakeSpark = SPARK.__versions.get();

       var sparkByVersion = SPARK.__versions.get('{{spark_gulp_build_version}}');

       var fakeByVersion = SPARK.__versions.get('0.0.1');

       expect(fakeByVersion).not.toEqual(latestRealSpark);
       expect(fakeByVersion).toEqual(latestFakeSpark);
       expect(sparkByVersion).toEqual(latestRealSpark);

       // has to restore the real version on top of the stack by re-adding
       // because some other tests expect to get the latest version by __versions.get()
       SPARK.__versions.add(latestRealSpark);
   });

});