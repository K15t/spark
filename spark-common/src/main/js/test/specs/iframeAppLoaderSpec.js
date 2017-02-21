describe('iframeAppLoader', function() {

   beforeEach(function() {

       AJS.testControl.initOnce();

       this.iframeAppLoader = SPARK.iframeAppLoader;

   });

   it('exists', function() {

       expect(this.iframeAppLoader).toEqual(jasmine.any(Object));

   });

   it('has openFullScreenIframeDialog function', function() {

      expect(this.iframeAppLoader.openFullscreenIframeDialog)
          .toEqual(jasmine.any(Function));

   });

});