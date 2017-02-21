describe('iframeAppLoader', function() {

   beforeEach(function() {

       AJS.testControl.initOnce();

       this.testControl = AJS.testControl;
       this.iframeAppLoader = SPARK.iframeAppLoader;

   });

   it('exists', function() {

       expect(this.iframeAppLoader).toEqual(jasmine.any(Object));

   });

   it('has openFullScreenIframeDialog function', function() {

      expect(this.iframeAppLoader.openFullscreenIframeDialog)
          .toEqual(jasmine.any(Function));

   });

   describe('openFullScreenIframeDialog', function() {

      beforeEach(function() {

          this.iframeTemplate = spyOn(SPARK.Common.Templates, 'appFullscreenContaineriFrame')
              .and.callThrough();

          this.iframeOpener = this.iframeAppLoader.openFullscreenIframeDialog;

          this.$ready = spyOn($.fn, 'ready');

      });

      afterEach(function() {

          $('body').empty();
          $('body').removeAttr('class');

      });


      it('uses the appFullscreenContaineriFrame template', function() {

          this.iframeOpener();

          expect(this.iframeTemplate).toHaveBeenCalled();

      });

      it('passes correct arguments to iframe template', function() {

          this.testControl.contextPath = '/test/context';
          spyOn(AJS, 'contextPath').and.callThrough();

          // location is bit cumbersome to mock, but its attributes should be same here as in tested path
          var expSrc = location.protocol + '//' + location.host +
                  '/test/context/test/app/path';

          this.iframeOpener('test-app-name', '/test/app/path');

          expect(this.iframeTemplate).toHaveBeenCalledWith({
              'id': 'test-app-name-spark-app-container',
              'src': expSrc,
              'createOptions': {'addChrome': false}
          });

      });

      it('removes scrollers from content below dialog', function() {

         var bodyEl = $('body');

         expect(bodyEl.hasClass('spark-no-scroll')).toBeFalsy();

         this.iframeOpener('test-app', '/test/path');

         expect(bodyEl.hasClass('spark-no-scroll')).toBeTruthy();

      });

      it('adds the iframe wrapper element to dom', function() {

          this.iframeOpener('test-app-name', '/test/app/path');

          var element = $('#test-app-name-spark-app-container');

          expect(element).toBeDefined();


      });

      it('adds a callback to be called once the iframe is loaded', function() {

          this.iframeOpener('test-app-name', '/test/app/path');

          expect(this.$ready).toHaveBeenCalled();

          expect(this.$ready).toHaveBeenCalledTimes(1);

          expect(this.$ready).toHaveBeenCalledWith(jasmine.any(Function));

      });

      describe('Iframe context handling', function() {

          beforeEach(function() {

              // for this test case the template result has to actually include an iframe
              // element, so that the callback can be triggered and it can find
              // an iframe element
              this.iframeTemplate.and.returnValue(
                  "<div id='iframe_test_el'><iframe></iframe></div>");


              this.getIframeContentWindow = function() {
                  var iframeEl = $('body').find('iframe');

                  expect(iframeEl).toBeDefined();

                  var iframeDomEl = iframeEl.get()[0];
                  expect(iframeDomEl).toBeDefined();

                  var iw = iframeDomEl.contentWindow;
                  expect(iw).toBeDefined();

                  return iw;
              };

              this.runReadyCb = function() {

                  // simulate the iframe becoming ready
                  var readyCallback = this.$ready.calls.argsFor(0)[0];
                  expect(readyCallback).toEqual(jasmine.any(Function));

                  readyCallback.call();

              };

          });

          it('the callback adds the SPARK controls to the iframe', function() {

              this.iframeOpener('test-app-name', '/test/app/path');

              var iw = this.getIframeContentWindow();

              // the execution of the ready function is controlled through the spy here
              // test that SPARK not added before 'ready'
              expect(iw.SPARK).not.toBeDefined();

              this.runReadyCb();

              // should have no added the SPARK context
              expect(iw.SPARK).toBeDefined();

          });

          it('adds the dialog close method to the iframe context', function() {

              this.iframeOpener('test-app-name', '/test/app/path');

              var iw = this.getIframeContentWindow();

              this.runReadyCb();

              // should have no added the SPARK context
              expect(iw.SPARK).toBeDefined();

              expect(iw.SPARK.iframeControls).toEqual(jasmine.any(Object));

              expect(iw.SPARK.iframeControls.closeDialog).toEqual(jasmine.any(Function));

              // check in the case that there is nothing unexpected in the iframe context
              // - just the close method (dialogChrome is null by default, and the extraData
              // what it was in dialog options, ending up undefined when not specified)
              expect(iw.SPARK.iframeControls).toEqual({
                  'closeDialog': jasmine.any(Function),
                  'dialogChrome': null,
                  'extraData': undefined
              });

          });

          it('close-method restores main content scrollers', function() {

              this.iframeOpener('test-app-name', '/test/app/path');
              iw = this.getIframeContentWindow();
              this.runReadyCb();

              expect($('body').hasClass('spark-no-scroll')).toBeTruthy();

              iw.SPARK.iframeControls.closeDialog();

              expect($('body').hasClass('spark-no-scroll')).toBeFalsy();

          });

          it('close-method removes the dialog wrapper element', function() {
              this.iframeOpener('test-app-name', '/test/app/path');

              iw = this.getIframeContentWindow();

              // should have the expected 'iframe wrapper element'

              var iframeWrapperEl = $('body').find('#iframe_test_el');
              expect(iframeWrapperEl.length).toEqual(1);
              expect(iframeWrapperEl.find('iframe').length).toEqual(1);

              // run the iframe ready callback, the dialog elements should still be there
              this.runReadyCb();

              var iframeWrapperEl2 = $('body').find('#iframe_test_el');
              expect(iframeWrapperEl2.length).toEqual(1);
              expect(iframeWrapperEl2.find('iframe').length).toEqual(1);

              // call the close method, now the should iframe-dialog wrapper
              // should not be there anymore
              iw.SPARK.iframeControls.closeDialog();

              expect($('body').find('#iframe_test_el').length).toEqual(0);
              expect($('body').find('iframe').length).toEqual(0);

          });

      });


   });

});