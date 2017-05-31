import spark from '../../src/spark-bootstrap';
import sparkTemplates from '../../src/spark-common-templates';

describe('iframeAppLoader', function() {

    beforeEach(function() {

        this.testControl = AJS.testControl;

        this.iframeResizer = jasmine.createSpy('iFrameResize');

        this.iframeAppLoader = spark.initIframeAppLoader(this.iframeResizer);

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

            this.iframeTemplate = spyOn(sparkTemplates, 'appFullscreenContaineriFrame')
                .and.returnValue('<div class="spark-mock-template"><iframe></iframe></div>');

            this.iframeOpener = this.iframeAppLoader.openFullscreenIframeDialog;

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
                '/test/context/test/app/path/';

            this.iframeOpener('test-app-name', '/test/app/path/');

            expect(this.iframeTemplate).toHaveBeenCalledWith({
                'id': 'test-app-name-spark-app-container',
                'src': expSrc,
                'createOptions': { 'addChrome': false },
                'className': jasmine.any(String)
            });

        });

        it('passes query string correctly to iframe', function() {

            this.testControl.contextPath = '/test/context';
            spyOn(AJS, 'contextPath').and.callThrough();

            // location is bit cumbersome to mock, but its attributes should be same here as in tested path
            var expSrcBase = location.protocol + '//' + location.host +
                '/test/context/test/app/path/';

            this.iframeOpener('test-app-name', '/test/app/path/', {
                'queryString': 'testParam=2'
            });

            expect(this.iframeTemplate).toHaveBeenCalledWith({
                'id': jasmine.any(String),
                'src': expSrcBase + '?testParam=2',
                'createOptions': jasmine.any(Object),
                'className': jasmine.any(String)
            });

            this.iframeTemplate.calls.reset();

            this.iframeOpener('test-app-name', '/test/app/path/', {
                'queryString': '?testParam=42'
            });

            expect(this.iframeTemplate).toHaveBeenCalledWith({
                'id': jasmine.any(String),
                'src': expSrcBase + '?testParam=42',
                'createOptions': jasmine.any(Object),
                'className': jasmine.any(String)
            });

            this.iframeTemplate.calls.reset();

            this.iframeOpener('test-app-name', '/test/app/path/', {
                'queryString': '&testParam=42&otherParam=36'
            });

            expect(this.iframeTemplate).toHaveBeenCalledWith({
                'id': jasmine.any(String),
                'src': expSrcBase + '?testParam=42&otherParam=36',
                'createOptions': jasmine.any(Object),
                'className': jasmine.any(String)
            });

        });

        it('adds trailing slash to path if needed', function() {

            this.testControl.contextPath = '/test/context';
            spyOn(AJS, 'contextPath').and.callThrough();

            // location is bit cumbersome to mock, but its attributes should be same here as in tested path
            var expSrcContext = location.protocol + '//' + location.host +
                '/test/context';

            this.iframeTemplate.calls.reset();

            this.iframeOpener('test-app-name', '/test/app/path');

            expect(this.iframeTemplate).toHaveBeenCalledWith({
                'id': jasmine.any(String),
                'src': expSrcContext + '/test/app/path/',
                'createOptions': jasmine.any(Object),
                'className': jasmine.any(String)
            });

            this.iframeTemplate.calls.reset();

            this.iframeOpener('test-app-name', '/test/app/path', {
                'queryString': 'testParam=2'
            });

            expect(this.iframeTemplate).toHaveBeenCalledWith({
                'id': jasmine.any(String),
                'src': expSrcContext + '/test/app/path/?testParam=2',
                'createOptions': jasmine.any(Object),
                'className': jasmine.any(String)
            });

            this.iframeOpener('test-app-name', '/test/app/path/index.html', {
                'queryString': 'testParam=2'
            });

            expect(this.iframeTemplate).toHaveBeenCalledWith({
                'id': jasmine.any(String),
                'src': expSrcContext + '/test/app/path/index.html?testParam=2',
                'createOptions': jasmine.any(Object),
                'className': jasmine.any(String)
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

                expect(iframeDomEl.SPARK.dialogControls).toEqual(jasmine.any(Object));

                expect(iframeDomEl.SPARK.dialogControls.closeDialog).toEqual(jasmine.any(Function));

                // check in the case that there is nothing unexpected in the iframe context
                // - just the close method (dialogChrome is null by default, and the contextData
                // what it was in dialog options, ending up undefined when not specified)
                expect(iframeDomEl.SPARK.dialogControls).toEqual({
                    'closeDialog': jasmine.any(Function),
                    'dialogChrome': null
                });

                expect(iframeDomEl.SPARK.contextData).not.toBeDefined();

            });

            it('close-method restores main content scrollers', function() {

                this.iframeOpener('test-app-name', '/test/app/path');

                var iframeSparkCont = this.getSparkIframeContext();

                expect($('body').hasClass('spark-no-scroll')).toBeTruthy();

                iframeSparkCont.dialogControls.closeDialog();

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
                iframeSpark.dialogControls.closeDialog();

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

                iframeSpark.dialogControls.closeDialog(resultData);

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

                expect(iframeSpark.dialogControls).toEqual(jasmine.any(Object));

                expect(iframeSpark.contextData).toEqual(jasmine.any(Object));

                expect(iframeSpark.contextData).toEqual({
                    'some': 'random',
                    'extra': ['data', 'for', 'iframe']
                });

            });

            it('calls the iFrameResizer host window part', function() {

                this.iframeResizer.calls.reset();

                this.iframeOpener('test-app-name', '/test/app/path');

                expect(this.iframeResizer).toHaveBeenCalled();

                expect(this.iframeResizer).toHaveBeenCalledWith({
                    'autoResize': true,
                    'heightCalculationMethod': 'max',
                    maxHeight: 300,
                    scrolling: 'auto',
                    resizedCallback: jasmine.any(Function)
                }, jasmine.anything());

            });

            it('calls iFrameResizer.close on closing dialog', function() {

                var iframeResizer = jasmine.createSpyObj('iFrameResizer', ['close']);

                this.iframeOpener('test-app-name', '/test/app/path');

                var iframeDomEl = $('body').find('iframe').get()[0];

                expect(iframeDomEl).toBeDefined();

                iframeDomEl.iFrameResizer = iframeResizer;

                expect(iframeResizer.close).not.toHaveBeenCalled();

                var iframeSparkCont = this.getSparkIframeContext();
                iframeSparkCont.dialogControls.closeDialog();

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
                        },
                        'className': jasmine.any(String)
                    });

                    var iframeSpark = this.getSparkIframeContext();

                    expect(iframeSpark.dialogControls).toEqual({
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

                    expect(iframeSpark.dialogControls.dialogChrome.cancelBtn).toEqual(parentCancelEl);
                    expect(iframeSpark.dialogControls.dialogChrome.confirmBtn).toEqual(parentSubmitEl);

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
                    iframeSparkCont.dialogControls.closeDialog();

                    expect($('body')
                        .find('#test-app-spark-app-container-chrome-cancel').length).toEqual(0);
                    expect($('body')
                        .find('#test-app-spark-app-container-chrome-submit').length).toEqual(0);

                });

                it('works as expected also with dialog chrome', function() {

                    // wrap a bit more verifications into this test about things
                    // that are already tested without having the dialog chrome

                    // test also later that iFrameResizer is called as expected
                    this.iframeResizer.calls.reset();

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
                    expect(iframeSparkCont.dialogControls).toEqual({
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

                    var iframeDomEl = $('body').find('iframe').get()[0];
                    expect(iframeDomEl).toBeDefined();

                    expect(this.iframeResizer).toHaveBeenCalled();
                    expect(this.iframeResizer).toHaveBeenCalledWith({
                        'autoResize': true,
                        'heightCalculationMethod': 'max',
                        maxHeight: 249,
                        scrolling: 'auto',
                        resizedCallback: jasmine.any(Function)
                    }, iframeDomEl);

                    var iframeDomElResizerObj = jasmine.createSpyObj('iFrameResizer', ['close']);

                    iframeDomEl.iFrameResizer = iframeDomElResizerObj;

                    // close dialog and check that everything is cleaned up
                    iframeSparkCont.dialogControls.closeDialog();

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