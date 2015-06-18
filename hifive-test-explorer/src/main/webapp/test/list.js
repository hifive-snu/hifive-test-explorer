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


	//
	// Extend QUnit
	// Returns async() object which automatically fails in 1 seconds
	//
	QUnit.assert.timedAsync = function() {
		var done = this.async();
		var timer = setTimeout(function() {
			this.ok(false, 'Timeout!');
			done();
		}, 1000);

		return function() {
			clearTimeout(timer);
			done();
		};
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

		QUnit.module('TestResultListLogic');
		var logic = page.logic.TestResultListLogic;
		QUnit.test('getTestExecutionList', function(assert) {
			var done = assert.timedAsync();
			logic.getTestExecutionList(1).done(function(data) {
				assert.ok(typeof data === 'object', 'It is an object.');
				assert.ok(data.content !== undefined, 'It has "content" property.');
				done();
			});
		});
		QUnit.test('getScreenshotList', function(assert) {
			var done = assert.timedAsync();
			logic.getScreenshotList(1).done(function(data) {
				assert.ok(Array.isArray(data), 'It is an array.');
				if (data.length > 0) {
					assert.ok(data[0].id !== undefined, "It's element has its id");
				}
				done();
			});
		});

		QUnit.module('TestResultListController');
		var controller = page.controller.TestResultListController;
		var $sandbox = sandbox.$(sandbox.document);

		QUnit.test('.explorer-test-result click', function(assert) {
			var done = assert.async();
			var $parent = $sandbox.find('.explorer-collapsable[data-test-execution-id=15]');
			$parent.on('shown.bs.collapse', collapse);
			$parent.find('div[role=tabpanel]').collapse('show');

			function collapse() {
				assert.ok(true, 'Successfully opened collapsed menu!');
				var $elem = $parent.find('.explorer-test-result[data-screenshot-id=147]');
				$elem.on('click', click);
				$elem.trigger('click');
			}

			function click() {
				assert.ok(true, 'Successfully clicked a link!');
				done();
			}
		});
	});
}();
