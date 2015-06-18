(function() {
	var preventDoubleExpose = function(obj) {
		var tokens = obj.__name.split('.');
		var curr = window;
		for(var i in tokens) {
			token = tokens[i];
			next = curr[token];
			if (typeof(next) == 'undefined')
				return false;
			curr = next;
		}
		eval('delete window.' + obj.__name);
		return true;
	};

	window.h5.hack = {
		'preventDoubleExpose': preventDoubleExpose,
	};
})();
