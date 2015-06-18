+function() {
	//
	// Extend EventTarget
	// Execute a function only once per DOM
	//
	EventTarget.prototype.once = function(handler) {
		this.addEventListener('load', wrapper);
		function wrapper() {
			this.removeEventListener('load', wrapper);
			handler.apply(this, arguments);
		}
	};


	document.querySelector('iframe').once(function() {
		var sandbox = this.contentWindow;

		//
		// Watch DOM change of sandbox, take coverage report from iframe and
		// insert into test report page
		//
		var body = sandbox.document.body;
		body._appendChild = body.appendChild;
		body.appendChild = function(elem) {
			if (elem.childElementCount === 1 && elem.children[0].id === 'blanket-main') {
				document.body.appendChild(elem.children[0]);
			} else {
				body._appendChild.apply(body, arguments);
			}
		}


		//
		// Main test codes
		//
		var page = sandbox.hifive.test.explorer;
		var controller = page.controller.TestResultDiffController;
		window.c = controller;
		QUnit.module('TestResultDiffController');
		QUnit.test('__ready()', function(assert) {
			controller.__ready();
			assert.ok(true, 'Does nothing');
		});

		QUnit.test('_initEdgeOverlapping()', function(assert) {
			controller._initEdgeOverlapping(64, 147);
			assert.ok(true, 'Does nothing');
		});

		QUnit.test('_hideActualMode()', function(assert) {
			controller._hideActualMode();
			assert.ok(true, 'Does nothing');
		});

		QUnit.test('_hideExpectedMode()', function(assert) {
			controller._hideExpectedMode();
			assert.ok(true, 'Does nothing');
		});
	});
}();
