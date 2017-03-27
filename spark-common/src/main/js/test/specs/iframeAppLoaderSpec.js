describe('iframeAppLoader', function() {

    beforeEach(function() {

        AJS.testControl.initOnce();

        this.testControl = AJS.testControl;
        this.iframeAppLoader = SPARK.__versions.get().iframeAppLoader;

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

            var currSpark = SPARK.__versions.get();
            this.iframeTemplate = spyOn(currSpark.Common.Templates, 'appFullscreenContaineriFrame')
                .and.returnValue('<div class="spark-mock-template"><iframe></iframe></div>');

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
                '/test/context/test/app/path/?iframe_content=true';

            this.iframeOpener('test-app-name', '/test/app/path/');

            expect(this.iframeTemplate).toHaveBeenCalledWith({
                'id': 'test-app-name-spark-app-container',
                'src': expSrc,
                'createOptions': { 'addChrome': false }
            });

        });

        it('passes query string correctly to iframe', function() {

            this.testControl.contextPath = '/test/context';
            spyOn(AJS, 'contextPath').and.callThrough();

            // location is bit cumbersome to mock, but its attributes should be same here as in tested path
            var expSrcBase = location.protocol + '//' + location.host +
                '/test/context/test/app/path/?iframe_content=true';

            this.iframeOpener('test-app-name', '/test/app/path/', {
                'queryString': 'testParam=2'
            });

            expect(this.iframeTemplate).toHaveBeenCalledWith({
                'id': jasmine.any(String),
                'src': expSrcBase + '&testParam=2',
                'createOptions': jasmine.any(Object)
            });

            this.iframeTemplate.calls.reset();

            this.iframeOpener('test-app-name', '/test/app/path/', {
                'queryString': '?testParam=42'
            });

            expect(this.iframeTemplate).toHaveBeenCalledWith({
                'id': jasmine.any(String),
                'src': expSrcBase + '&testParam=42',
                'createOptions': jasmine.any(Object)
            });

            this.iframeTemplate.calls.reset();

            this.iframeOpener('test-app-name', '/test/app/path/', {
                'queryString': '&testParam=42&otherParam=36'
            });

            expect(this.iframeTemplate).toHaveBeenCalledWith({
                'id': jasmine.any(String),
                'src': expSrcBase + '&testParam=42&otherParam=36',
                'createOptions': jasmine.any(Object)
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

        describe('Iframe context handling', function() {

            beforeEach(function() {

                // for this test case the template result has to actually include an iframe
                // element, so that the callback can be triggered and it can find
                // an iframe element
                this.iframeTemplate.and.returnValue(
                    '<div id="iframe_test_el"><iframe></iframe></div>'
                );

                this.getIframeContentWindow = function() {
                    var iframeDomEl = this.getIframeDomEl();

                    var iw = iframeDomEl.contentWindow;
                    expect(iw).toBeDefined();

                    return iw;
                };

                this.getIframeDomEl = function() {

                    var iframeEl = $('body').find('iframe');

                    expect(iframeEl).toBeDefined();

                    var iframeDomEl = iframeEl.get()[0];
                    expect(iframeDomEl).toBeDefined();

                    return iframeDomEl;
                };

                this.getSparkIframeContext = function() {

                    var iframeDomEl = this.getIframeDomEl();

                    return iframeDomEl.SPARK;

                };

                this.addIframeResizerMock = function() {
                    var iframeResizer = jasmine.createSpy('iFrameResize');

                    $.fn.iFrameResize = iframeResizer;

                    return iframeResizer;
                };

            });

            it('the callback adds the SPARK controls to the iframe element', function() {

                this.iframeOpener('test-app-name', '/test/app/path');

                var iframeDomEl = this.getIframeDomEl();

                // should have no added the SPARK context
                expect(iframeDomEl.SPARK).toBeDefined();

            });

            it('adds the dialog close method to the iframe context', function() {

                this.iframeOpener('test-app-name', '/test/app/path');

                var iframeDomEl = this.getIframeDomEl();

                // should have no added the SPARK context
                expect(iframeDomEl.SPARK).toBeDefined();

                expect(iframeDomEl.SPARK.iframeControls).toEqual(jasmine.any(Object));

                expect(iframeDomEl.SPARK.iframeControls.closeDialog).toEqual(jasmine.any(Function));

                // check in the case that there is nothing unexpected in the iframe context
                // - just the close method (dialogChrome is null by default, and the contextData
                // what it was in dialog options, ending up undefined when not specified)
                expect(iframeDomEl.SPARK.iframeControls).toEqual({
                    'closeDialog': jasmine.any(Function),
                    'dialogChrome': null
                });

                expect(iframeDomEl.SPARK.contextData).not.toBeDefined();

            });

            it('close-method restores main content scrollers', function() {

                this.iframeOpener('test-app-name', '/test/app/path');

                var iframeSparkCont = this.getSparkIframeContext();

                expect($('body').hasClass('spark-no-scroll')).toBeTruthy();

                iframeSparkCont.iframeControls.closeDialog();

                expect($('body').hasClass('spark-no-scroll')).toBeFalsy();

            });

            it('close-method removes the dialog wrapper element', function() {
                this.iframeOpener('test-app-name', '/test/app/path');

                var iframeSpark = this.getSparkIframeContext();

                // should have the expected 'iframe wrapper element'

                var iframeWrapperEl = $('body').find('#iframe_test_el');
                expect(iframeWrapperEl.length).toEqual(1);
                expect(iframeWrapperEl.find('iframe').length).toEqual(1);

                // call the close method, now the should iframe-dialog wrapper
                // should not be there anymore
                iframeSpark.iframeControls.closeDialog();

                expect($('body').find('#iframe_test_el').length).toEqual(0);
                expect($('body').find('iframe').length).toEqual(0);

            });

            it('close-method invokes the onClose handler with result data', function() {
                var closeCallback = jasmine.createSpy('dialogCloseCallback');
                this.iframeOpener('test-app-name', '/test/app/path', { onClose: closeCallback });

                var iframeSpark = this.getSparkIframeContext();

                var iframeResizer = jasmine.createSpyObj('iFrameResizer', ['close']);
                var iframeDomEl = $('body').find('iframe').get()[0];
                expect(iframeDomEl).toBeDefined();
                iframeDomEl.iFrameResizer = iframeResizer;

                expect(iframeResizer.close).not.toHaveBeenCalled();

                var resultData = {
                    a: 'b', c: function() {
                    }
                };

                iframeSpark.iframeControls.closeDialog(resultData);

                expect(closeCallback).toHaveBeenCalledTimes(1);
                expect(closeCallback).toHaveBeenCalledWith(resultData);
            });

            it('passes extra context data to iframe context', function() {

                var contextData = {
                    'some': 'random',
                    'extra': ['data', 'for', 'iframe']
                };

                this.iframeOpener('test-app-name', '/test/app/path', {
                    'contextData': contextData
                });

                var iframeSpark = this.getSparkIframeContext();

                expect(iframeSpark.iframeControls).toEqual(jasmine.any(Object));

                expect(iframeSpark.contextData).toEqual(jasmine.any(Object));

                expect(iframeSpark.contextData).toEqual({
                    'some': 'random',
                    'extra': ['data', 'for', 'iframe']
                });

            });

            it('calls the iFrameResizer host window part', function() {

                var iframeResizer = this.addIframeResizerMock();

                this.iframeOpener('test-app-name', '/test/app/path');

                expect(iframeResizer).toHaveBeenCalled();

                expect(iframeResizer).toHaveBeenCalledWith([{
                    'autoResize': true,
                    'heightCalculationMethod': 'max'
                }]);

            });

            it('calls iFrameResizer.close on closing dialog', function() {

                var iframeResizer = jasmine.createSpyObj('iFrameResizer', ['close']);

                this.iframeOpener('test-app-name', '/test/app/path');

                var iframeDomEl = $('body').find('iframe').get()[0];

                expect(iframeDomEl).toBeDefined();

                iframeDomEl.iFrameResizer = iframeResizer;

                expect(iframeResizer.close).not.toHaveBeenCalled();

                var iframeSparkCont = this.getSparkIframeContext();
                iframeSparkCont.iframeControls.closeDialog();

                expect(iframeResizer.close).toHaveBeenCalledTimes(1);

            });

            describe('dialog chrome handling', function() {

                beforeEach(function() {

                    // this has to match the id prefix used for the dialog buttons
                    this.appNameToUse = 'test-app';

                    // a template including the dialog chrome is needed for these test cases
                    this.iframeTemplate.and.returnValue(
                        '<div id="iframe_test_el">' +
                        '   <div class="dialog-chrome">' +
                        '   <button id="test-app-spark-app-container-chrome-submit">Submit</button>' +
                        '   <button id="test-app-spark-app-container-chrome-cancel">Cancel</button>' +
                        '</div>' +
                        '<iframe></iframe></div>'
                    );

                });

                it('adds chrome (and control object) when asked', function() {

                    this.iframeOpener(this.appNameToUse, '/path/to/app', {
                        'addChrome': true
                    });

                    // check that the 'addChrome': true is forwarded to the template

                    expect(this.iframeTemplate).toHaveBeenCalledWith({
                        'id': 'test-app-spark-app-container',
                        'src': jasmine.any(String),
                        'createOptions': {
                            'addChrome': true
                        }
                    });

                    var iframeSpark = this.getSparkIframeContext();

                    expect(iframeSpark.iframeControls).toEqual({
                        'closeDialog': jasmine.any(Function),
                        'dialogChrome': {
                            'cancelBtn': jasmine.any(HTMLElement),
                            'confirmBtn': jasmine.any(HTMLElement)
                        }
                    });

                    var parentCancelEl = $('body')
                        .find('#test-app-spark-app-container-chrome-cancel').get()[0];
                    var parentSubmitEl = $('body')
                        .find('#test-app-spark-app-container-chrome-submit').get()[0];

                    expect(parentCancelEl).toBeDefined();
                    expect(parentSubmitEl).toBeDefined();

                    expect(iframeSpark.iframeControls.dialogChrome.cancelBtn).toEqual(parentCancelEl);
                    expect(iframeSpark.iframeControls.dialogChrome.confirmBtn).toEqual(parentSubmitEl);

                });

                it('removes the dialog chrome on close', function() {

                    this.iframeOpener(this.appNameToUse, '/path/to/app', {
                        'addChrome': true
                    });

                    expect($('body')
                        .find('#test-app-spark-app-container-chrome-cancel').length).toEqual(1);
                    expect($('body')
                        .find('#test-app-spark-app-container-chrome-submit').length).toEqual(1);

                    var iframeSparkCont = this.getSparkIframeContext();
                    iframeSparkCont.iframeControls.closeDialog();

                    expect($('body')
                        .find('#test-app-spark-app-container-chrome-cancel').length).toEqual(0);
                    expect($('body')
                        .find('#test-app-spark-app-container-chrome-submit').length).toEqual(0);

                });

                it('works as expected also with dialog chrome', function() {

                    // wrap a bit more verifications into this test about things
                    // that are already tested without having the dialog chrome

                    // test also later that iFrameResizer is called as expected
                    var iframeResizer = this.addIframeResizerMock();

                    expect($('body').hasClass('spark-no-scroll')).toBeFalsy();
                    expect($('body').find('iframe').length).toEqual(0);

                    this.iframeOpener(this.appNameToUse, '/path/to/app/', {
                        'addChrome': true,
                        'contextData': { 'test': 'some extra', 'with': { 'chrome': true } }
                    });

                    // chrome added
                    expect($('body')
                        .find('#test-app-spark-app-container-chrome-cancel').length).toEqual(1);
                    expect($('body')
                        .find('#test-app-spark-app-container-chrome-submit').length).toEqual(1);

                    // scrollers blocked, iframe added
                    expect($('body').hasClass('spark-no-scroll')).toBeTruthy();
                    expect($('body').find('iframe').length).toEqual(1);

                    var iframeSparkCont = this.getSparkIframeContext();

                    // added expected data to iframe's contextwindow
                    expect(iframeSparkCont).toEqual(jasmine.any(Object));
                    expect(iframeSparkCont.iframeControls).toEqual({
                        'closeDialog': jasmine.any(Function),
                        'dialogChrome': {
                            'cancelBtn': jasmine.any(HTMLElement),
                            'confirmBtn': jasmine.any(HTMLElement)
                        }
                    });
                    expect(iframeSparkCont.contextData).toEqual({
                        'test': 'some extra',
                        'with': { 'chrome': true }
                    });

                    expect(iframeResizer).toHaveBeenCalled();
                    expect(iframeResizer).toHaveBeenCalledWith([{
                        'autoResize': true,
                        'heightCalculationMethod': 'max'
                    }]);

                    var iframeDomElResizerObj = jasmine.createSpyObj('iFrameResizer', ['close']);

                    var iframeDomEl = $('body').find('iframe').get()[0];
                    expect(iframeDomEl).toBeDefined();

                    iframeDomEl.iFrameResizer = iframeDomElResizerObj;

                    // close dialog and check that everything is cleaned up
                    iframeSparkCont.iframeControls.closeDialog();

                    expect($('body')
                        .find('#test-app-spark-app-container-chrome-cancel').length).toEqual(0);
                    expect($('body')
                        .find('#test-app-spark-app-container-chrome-submit').length).toEqual(0);

                    expect($('body').hasClass('spark-no-scroll')).toBeFalsy();
                    expect($('body').find('iframe').length).toEqual(0);

                    expect(iframeDomElResizerObj.close).toHaveBeenCalled();

                });

            });

        });


    });

});