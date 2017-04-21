import globalSpark from '../../src/global';

describe('No conflict version handling', function() {

   it('adds getSpark global function', function() {

        expect(window.getSpark).toEqual(jasmine.any(Function));

   });

   it('calling getSpark too late fails', function(done) {
       window.setTimeout(function() {
           expect(window.getSpark).toThrow();
           done();
       }, 10);
   });

   it('does not override old SPARK global object', function() {

       expect(SPARK).toEqual(AJS.testControl.oldSparkMockupVersion);

       expect(SPARK.mockupOldVersion).toEqual(true);

       expect(SPARK.appLoader2).not.toBeDefined();
       expect(SPARK.iframeAppLoader).not.toBeDefined();

   });

   it('does not add iFrameResize to global jQuery', function() {

       expect(window.jQuery.fn.iFrameResize).not.toBeDefined();

   });

});