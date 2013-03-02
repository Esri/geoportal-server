
if(typeof(window._mapFlexViewer) == 'undefined') {
	window._mapFlexViewer = null;
}

try {

	window._mapFlexViewer = window.open(
					'',
					'{windowTitle}',
					'width={width},height={height},outerWidth={width},outerHeight={height},resizable,scrollbars,location=0');
	if (typeof (window._mapFlexViewer) == 'undefined' || window._mapFlexViewer== null
			|| typeof (_mapFlexViewer.addResource) == 'undefined') {
	
		window._mapFlexViewer = window.open(
						'{mapAndResourceUrl}',
						'{windowTitle}',
						'width={width},height={height},outerWidth={width},outerHeight={height},resizable,scrollbars,location=0');
		window._mapFlexViewer.window_handle = self;
		window._mapFlexViewer.window_handle.closed = false;
	} else if (true == true) { 
	
		window._mapFlexViewer.addResource('{title}', '{jsResourceUrl}');
	}
	
} catch (err) {
	console.log(err);
	window._mapFlexViewer = window.open(
					'{mapAndResourceUrl}',
					'{windowTitle}',
					'width={outerwidth},height={height},outerWidth={width},outerHeight={height},resizable,scrollbars,location=0');

}
window._mapFlexViewer.focus();



return false;