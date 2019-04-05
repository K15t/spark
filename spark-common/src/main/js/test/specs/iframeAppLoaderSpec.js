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

    it('has openInlineIframeDialog function', function() {

        expect(this.iframeAppLoader.openInlineIframeDialog)
            .toEqual(jasmine.any(Function));

    });

    it('has createAppIframe function', function() {

        expect(this.iframeAppLoader.createAppIframe)
            .toEqual(jasmine.any(Function));

    });

    describe('openFullScreenIFrameDialog', function() {

        var FS_DIALOG_SPARK_CONTAINER_MOCK_ID = 'test-app-name-spark-app-container';

        beforeEach(function() {

            this.iframeTemplate = spyOn(sparkTemplates, 'appFullscreenContainerIframe')
                .and.returnValue('<div id="' + FS_DIALOG_SPARK_CONTAINER_MOCK_ID+ '" class="spark-mock-template"><iframe></iframe></div>');

            this.fullScreenDialogOpener = this.iframeAppLoader.openFullscreenIframeDialog;

        });

        afterEach(function() {

            $('body').empty();
            $('body').removeAttr('class');

        });


        it('throws proper exception when no/invalid parameters are provided', function() {
            expect(() => {
                this.fullScreenDialogOpener()
            }).toThrowError('Parameter missing - \'appName\'');
            expect(() => {
                this.fullScreenDialogOpener('test-app-name')
            }).toThrowError('Parameter missing - \'appPath\'');
        });

        it('returns the spark app container id when valid parameters are provided', function() {
            expect(this.fullScreenDialogOpener('test-app-name', '/test/app/path/', {})).toEqual({
                appContainerId: 'test-app-name-spark-app-container',
                iframeDomEl: jasmine.any(HTMLElement),
                iframeSparkContext: jasmine.anything()
            });
        });

        it('removes scrollers from content below dialog', function() {

            var bodyEl = $('body');

            expect(bodyEl.hasClass('spark-no-scroll')).toBeFalsy();

            this.fullScreenDialogOpener('test-app', '/test/path');

            expect(bodyEl.hasClass('spark-no-scroll')).toBeTruthy();

        });

        it('uses the appFullscreenContainerIframe template', function() {

            this.fullScreenDialogOpener('test-app-name', '/test/app/path/', {});

            expect(this.iframeTemplate).toHaveBeenCalled();

        });

        it('calls appFullscreenContainerIframe with correct parameters', function() {

            this.testControl.contextPath = '/test/context';
            spyOn(AJS, 'contextPath').and.callThrough();

            // location is bit cumbersome to mock, but its attributes should be same here as in tested path
            var expSrc = location.protocol + '//' + location.host +
                '/test/context/test/app/path/';

            this.fullScreenDialogOpener('test-app-name', '/test/app/path/');

            expect(this.iframeTemplate).toHaveBeenCalledWith({
                'id': 'test-app-name-spark-app-container',
                'createOptions': { 'addChrome': false },
                'className': jasmine.any(String),
                'src': expSrc
            });
        });

        it('adds the iframe wrapper element to dom', function() {

            this.fullScreenDialogOpener('test-app-name', '/test/app/path', {});

            var element = $('#' + FS_DIALOG_SPARK_CONTAINER_MOCK_ID);

            expect(element.length).toEqual(1);

        });

        describe('Iframe context handling', function() {

            beforeEach(function() {

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

                this.fullScreenDialogOpener('test-app-name', '/test/app/path', {
                    'width': '100px'
                });

                var iframeDomEl = this.getIframeDomEl();

                // should have no added the SPARK context
                expect(iframeDomEl.SPARK).toBeDefined();

            });

            it('adds the dialog close method to the iFrame context', function() {

                this.fullScreenDialogOpener('test-app-name', '/test/app/path');

                var iframeDomEl = this.getIframeDomEl();

                // should have added the SPARK context
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

                expect(iframeDomEl.SPARK.customContext).not.toBeDefined();

            });

            it('close-method restores main content scrollers', function() {

                this.fullScreenDialogOpener('test-app-name', '/test/app/path', {});

                var iframeSparkCont = this.getSparkIframeContext();

                expect($('body').hasClass('spark-no-scroll')).toBeTruthy();

                iframeSparkCont.dialogControls.closeDialog();

                expect($('body').hasClass('spark-no-scroll')).toBeFalsy();

            });

            it('close-method removes the dialog wrapper element', function() {
                this.fullScreenDialogOpener('test-app-name', '/test/app/path', {});

                var iframeSpark = this.getSparkIframeContext();

                // should have the expected 'iframe wrapper element'

                var iframeWrapperEl = $('body').find('#' + FS_DIALOG_SPARK_CONTAINER_MOCK_ID);
                expect(iframeWrapperEl.length).toEqual(1);
                expect(iframeWrapperEl.find('iframe').length).toEqual(1);

                // call the close method, after that the iframe-dialog wrapper should not be there anymore
                iframeSpark.dialogControls.closeDialog();

                expect($('body').find('#' + FS_DIALOG_SPARK_CONTAINER_MOCK_ID).length).toEqual(0);
                expect($('body').find('iframe').length).toEqual(0);

            });

            it('close-method invokes the onClose handler with result data', function() {
                var closeCallback = jasmine.createSpy('dialogCloseCallback');
                this.fullScreenDialogOpener('test-app-name', '/test/app/path', { onClose: closeCallback });

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

            it('calls iFrameResizer.close on closing dialog', function() {

                var iframeResizer = jasmine.createSpyObj('iFrameResizer', ['close']);

                this.fullScreenDialogOpener('test-app-name', '/test/app/path', {
                    'width': '100px',
                });

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
                    this.appNameToUse = 'test';

                    // a template including the dialog chrome is needed for these test cases
                    this.iframeTemplate.and.returnValue(
                        '<div id="test-spark-app-container">' +
                        '   <div class="dialog-chrome">' +
                        '       <button id="test-spark-app-container-chrome-submit">Submit</button>' +
                        '       <button id="test-spark-app-container-chrome-cancel">Cancel</button>' +
                        '   </div>' +
                        '   <div><iframe></iframe></div>' +
                        '</div>'
                    );


                });

                it('adds chrome (and control object) when asked', function() {

                    this.fullScreenDialogOpener(this.appNameToUse, '/path/to/app', {
                        'addChrome': true
                    });

                    // check that the 'addChrome': true is forwarded to the template

                    expect(this.iframeTemplate).toHaveBeenCalledWith({
                        'id': 'test-spark-app-container',
                        'createOptions': {
                            'addChrome': true
                        },
                        'className': jasmine.any(String),
                        'src': jasmine.any(String)
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
                        .find('#test-spark-app-container-chrome-cancel').get()[0];
                    var parentSubmitEl = $('body')
                        .find('#test-spark-app-container-chrome-submit').get()[0];

                    expect(parentCancelEl).toBeDefined();
                    expect(parentSubmitEl).toBeDefined();

                    expect(iframeSpark.dialogControls.dialogChrome.cancelBtn).toEqual(parentCancelEl);
                    expect(iframeSpark.dialogControls.dialogChrome.confirmBtn).toEqual(parentSubmitEl);

                });

                it('removes the dialog chrome on close', function() {

                    this.fullScreenDialogOpener(this.appNameToUse, '/path/to/app', {
                        'addChrome': true
                    });

                    expect($('body')
                        .find('#test-spark-app-container-chrome-cancel').length).toEqual(1);
                    expect($('body')
                        .find('#test-spark-app-container-chrome-submit').length).toEqual(1);

                    var iframeSparkCont = this.getSparkIframeContext();
                    iframeSparkCont.dialogControls.closeDialog();

                    expect($('body')
                        .find('#test-spark-app-container-chrome-cancel').length).toEqual(0);
                    expect($('body')
                        .find('#test-spark-app-container-chrome-submit').length).toEqual(0);

                });

                it('works as expected also with dialog chrome', function() {

                    // wrap a bit more verifications into this test about things
                    // that are already tested without having the dialog chrome

                    // test also later that iframeResizer is called as expected
                    this.iframeResizer.calls.reset();

                    expect($('body').hasClass('spark-no-scroll')).toBeFalsy();
                    expect($('body').find('iframe').length).toEqual(0);

                    this.fullScreenDialogOpener(this.appNameToUse, '/path/to/app/', {
                        'addChrome': true,
                        'contextData': { 'test': 'some extra', 'with': { 'chrome': true } }
                    });

                    // chrome added
                    expect($('body')
                        .find('#test-spark-app-container-chrome-cancel').length).toEqual(1);
                    expect($('body')
                        .find('#test-spark-app-container-chrome-submit').length).toEqual(1);

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
                        maxHeight: window.innerHeight - 51, // innerHeight - chrome bar
                        scrolling: 'auto',
                        resizedCallback: jasmine.any(Function)
                    }, iframeDomEl);

                    var iframeDomElResizerObj = jasmine.createSpyObj('iFrameResizer', ['close']);

                    iframeDomEl.iFrameResizer = iframeDomElResizerObj;

                    // close dialog and check that everything is cleaned up
                    iframeSparkCont.dialogControls.closeDialog();

                    expect($('body')
                        .find('#test-spark-app-container-chrome-cancel').length).toEqual(0);
                    expect($('body')
                        .find('#test-spark-app-container-chrome-submit').length).toEqual(0);

                    expect($('body').hasClass('spark-no-scroll')).toBeFalsy();
                    expect($('body').find('iframe').length).toEqual(0);

                    expect(iframeDomElResizerObj.close).toHaveBeenCalled();

                });

            });

            it('passes extra context data to iframe context', function() {

                var contextData = {
                    'some': 'random',
                    'extra': ['data', 'for', 'iframe']
                };

                this.fullScreenDialogOpener('test-app-name', '/test/app/path', {
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

            it('passes custom context data to iframe context', function() {

                var customContext = {
                    'foo': 'bar'
                };

                this.fullScreenDialogOpener('test-app-name', '/test/app/path', {
                    'customContext': customContext
                });

                var iframeSpark = this.getSparkIframeContext();

                expect(iframeSpark.customContext).toEqual({
                    'foo': 'bar'
                });

            });

        });

    });

    describe('create Iframe', function() {
        beforeEach(function() {
            this.bootstrappedIframe = spyOn(sparkTemplates, 'bootstrappedIframe')
                .and.returnValue('<iframe></iframe>');

            this.iframeCreator = this.iframeAppLoader.createAppIframe;
        });


        it('throws proper exception when no/invalid parameters are provided', function() {

            expect(() => {
                this.iframeCreator()
            }).toThrowError('Parameter missing - \'appId\'');
            expect(() => {
                this.iframeCreator('test-app-id')
            }).toThrowError('Parameter missing - \'appPath\'');
            expect(() => {
                this.iframeCreator('test-app-id', 'test/app/path/')
            }).toThrowError('Parameter missing - \'options\'');
        });

        it('returns iframe element and iframe Spark context when valid parameters are provided', function() {
            expect(this.iframeCreator('test-app-id', '/test/app/path/', {})).toEqual(
                { iframeDomEl: jasmine.any(HTMLElement), iframeSparkContext: jasmine.any(Object),
                    appContainerId: 'test-app-id-spark-iframe' });
        });

        it('passes query string parameters to iframe correctly ', function() {

            this.testControl.contextPath = '/test/context';
            spyOn(AJS, 'contextPath').and.callThrough();

            // location is bit cumbersome to mock, but its attributes should be same here as in tested path
            var expSrcBase = location.protocol + '//' + location.host +
                '/test/context/test/app/path/';

            this.iframeCreator('test-app-id', '/test/app/path/', {
                'queryString': 'testParam=2'
            });

            expect(this.bootstrappedIframe).toHaveBeenCalledWith({
                'id': jasmine.any(String),
                'src': expSrcBase + '?testParam=2',
                'className': jasmine.any(String)
            });

            this.bootstrappedIframe.calls.reset();

            this.iframeCreator('test-app-id', '/test/app/path/', {
                'queryString': '?testParam=42'
            });

            expect(this.bootstrappedIframe).toHaveBeenCalledWith({
                'id': jasmine.any(String),
                'src': expSrcBase + '?testParam=42',
                'className': jasmine.any(String)
            });

            this.bootstrappedIframe.calls.reset();

            this.iframeCreator('test-app-id', '/test/app/path/', {
                'queryString': '&testParam=42&otherParam=36'
            });

            expect(this.bootstrappedIframe).toHaveBeenCalledWith({
                'id': jasmine.any(String),
                'src': expSrcBase + '?testParam=42&otherParam=36',
                'className': jasmine.any(String)
            });

        });

        it('adds trailing slash to app path if needed', function() {

            this.testControl.contextPath = '/test/context';
            spyOn(AJS, 'contextPath').and.callThrough();

            // location is bit cumbersome to mock, but its attributes should be same here as in tested path
            var expSrcContext = location.protocol + '//' + location.host +
                '/test/context';

            this.bootstrappedIframe.calls.reset();

            this.iframeCreator('test-app-id', '/test/app/path', {});

            expect(this.bootstrappedIframe).toHaveBeenCalledWith({
                'id': jasmine.any(String),
                'src': expSrcContext + '/test/app/path/',
                'className': jasmine.any(String)
            });

            this.bootstrappedIframe.calls.reset();

            this.iframeCreator('test-app-id', '/test/app/path', {
                'queryString': 'testParam=2'
            });

            expect(this.bootstrappedIframe).toHaveBeenCalledWith({
                'id': jasmine.any(String),
                'src': expSrcContext + '/test/app/path/?testParam=2',
                'className': jasmine.any(String)
            });

            this.iframeCreator('test-app-id', '/test/app/path/index.html', {
                'queryString': 'testParam=2'
            });

            expect(this.bootstrappedIframe).toHaveBeenCalledWith({
                'id': jasmine.any(String),
                'src': expSrcContext + '/test/app/path/index.html?testParam=2',
                'className': jasmine.any(String)
            });

        });

        it('calls bootstrappedIframe with correct parameters', function() {
            this.testControl.contextPath = '/test/context';
            spyOn(AJS, 'contextPath').and.callThrough();

            // location is bit cumbersome to mock, but its attributes should be same here as in tested path
            var expSrcContext = location.protocol + '//' + location.host +
                '/test/context';

            this.iframeCreator('test-app-id', '/test/app/path/', { test: 'options' });
            expect(this.bootstrappedIframe).toHaveBeenCalledWith(
                {
                    'id': 'test-app-id-spark-iframe',
                    'src': expSrcContext + '/test/app/path/',
                    'className': jasmine.any(String)
                }
            );
        });

        describe('iframe context handling', function() {

            it('adds SPARK context data, resizeIFrameWidth function and  custom context data (when available) to the iframe', function() {

                expect(this.iframeCreator('test-app-id', '/test/app/path/', {
                    contextData: 'test-context-data'
                }).iframeDomEl.SPARK).toBeDefined();

                expect(this.iframeCreator('test-app-id', '/test/app/path/', {
                    contextData: 'test-context-data'
                }).iframeSparkContext).toEqual({
                    contextData: 'test-context-data',
                    setContainerWidth: jasmine.any(Function)
                });

                expect(this.iframeCreator('test-app-id', '/test/app/path/', {
                    contextData: 'test-context-data',
                    customContext: 'test-custom-context-data'
                }).iframeSparkContext).toEqual({
                    contextData: 'test-context-data',
                    customContext: 'test-custom-context-data',
                    setContainerWidth: jasmine.any(Function)
                });
            });
        });

        describe('iframe resizer handling', function() {
            it('calls the iframeResizer with default parameters', function() {

                this.iframeResizer.calls.reset();

                this.iframeCreator('test-app-id', '/test/app/path/', {});

                expect(this.iframeResizer).toHaveBeenCalled();

                expect(this.iframeResizer).toHaveBeenCalledWith({
                    'autoResize': true,
                    'heightCalculationMethod': 'max'
                }, jasmine.anything());

            });

            it('calls the iframeResizer with additional parameters when available', function() {

                this.iframeResizer.calls.reset();

                this.iframeCreator('test-app-id', '/test/app/path/', { iframeResizerSettings: { foo: 'bar' } });

                expect(this.iframeResizer).toHaveBeenCalled();

                expect(this.iframeResizer).toHaveBeenCalledWith({
                    'autoResize': true,
                    'heightCalculationMethod': 'max',
                    'foo': 'bar'
                }, jasmine.anything());

            });
        });


    });

    describe('openInlineIframeDialog', function() {
        beforeEach(function() {
            this.iframeTemplate = spyOn(sparkTemplates, 'inlineDialogAppContainer')
                .and.returnValue(
                    `<aui-inline-dialog id="test-app-name-spark-app-container" class="spark-mock-template">
                        <div id="test-app-name-spark-app-container"><iframe></iframe></div>
                 </aui-inline-dialog>`
                );


            this.bootstrappedIframe = spyOn(sparkTemplates, 'bootstrappedIframe')
                .and.returnValue('<iframe></iframe>');

            this.inlineDialogOpener = this.iframeAppLoader.openInlineIframeDialog;
        });

        it('throws proper exceptions when no/invalid parameters are provided', function() {

            expect(() => {
                this.inlineDialogOpener()
            }).toThrowError('Parameter missing - \'appName\'');
            expect(() => {
                this.inlineDialogOpener('test-app-name')
            }).toThrowError('Parameter missing - \'appPath\'');
        });

        it('returns the Spark app container Id when valid parameters are provided', function() {
            this.triggerElement = spyOn(sparkTemplates, 'inlineDialogTrigger')
                .and.returnValue(
                    `<a></a>`
                );
            expect(this.inlineDialogOpener('test-app-name', '/test/app/path/', {})).toEqual(
                { triggerEl: `<a></a>`, iframeSparkContext: jasmine.anything(),
                    iframeDomEl: jasmine.any(HTMLElement), appContainerId: 'test-app-name-spark-app-container'});
        });

        it('calls inlineDialogTrigger with default parameters', function() {
            this.triggerElement = spyOn(sparkTemplates, 'inlineDialogTrigger');
            this.inlineDialogOpener('test-app-name', '/test/app/path/', {});
            expect(this.triggerElement).toHaveBeenCalledWith({
                targetId: 'test-app-name-spark-app-container',
                text: 'Inline trigger'
            });
        });

        it('calls inlineDialogTrigger with correct parameters if when provided', function() {
            this.triggerElement = spyOn(sparkTemplates, 'inlineDialogTrigger');
            this.inlineDialogOpener('test-app-name', '/test/app/path/', { triggerText: 'testTriggerText' });
            expect(this.triggerElement).toHaveBeenCalledWith({
                'targetId': 'test-app-name-spark-app-container',
                'text': 'testTriggerText'
            });
        });

        it('calls inlineDialogAppContainer with correct parameters', function() {

            var expSrc = location.protocol + '//' + location.host +
                '/test/context/test/app/path/';

            this.inlineDialogOpener('test-app-name', '/test/app/path/', { foo: 'bar' });
            expect(this.iframeTemplate).toHaveBeenCalledWith({
                'id': 'test-app-name-spark-app-container',
                'createOptions': {
                    'width': '540px',
                    'triggerText': 'Inline trigger',
                    'alignment': 'bottom left',
                    'foo': 'bar'
                },
                'className': jasmine.any(String),
                'src': expSrc
            });
        });

        it('adds the iframe wrapper element to dom', function() {

            this.inlineDialogOpener('test-app-name', '/test/app/path', {});

            var element = $('#test-app-name-spark-app-container');

            expect(element).toBeDefined();


        });


        describe('Iframe context handling', function() {

            beforeEach(function() {

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

                this.inlineDialogOpener('test-app-name', '/test/app/path', {
                    'width': '100px'
                });

                var iframeDomEl = this.getIframeDomEl();

                // should have no added the SPARK context
                expect(iframeDomEl.SPARK).toBeDefined();

            });

            it('passes extra context data to iframe context', function() {

                var contextData = {
                    'some': 'random',
                    'extra': ['data', 'for', 'iframe']
                };

                this.inlineDialogOpener('test-app-name', '/test/app/path', {
                    'contextData': contextData
                });

                var iframeSpark = this.getSparkIframeContext();

                expect(iframeSpark.contextData).toEqual(jasmine.any(Object));

                expect(iframeSpark.contextData).toEqual({
                    'some': 'random',
                    'extra': ['data', 'for', 'iframe']
                });

            });

            it('passes custom context data to iframe context', function() {

                var customContext = {
                    'foo': 'bar'
                };

                this.inlineDialogOpener('test-app-name', '/test/app/path', {
                    'customContext': customContext
                });

                var iframeSpark = this.getSparkIframeContext();

                expect(iframeSpark.customContext).toEqual({
                    'foo': 'bar'
                });

            });

            it('checks the target of  the triggerElement is equal to the id of the dialogElement', function() {
                var { triggerEl } = this.inlineDialogOpener('test-app-name', '/test/app/path', {});

                expect($(triggerEl).attr('aria-controls')).toEqual('test-app-name-spark-app-container');

            });
        });
    });

});