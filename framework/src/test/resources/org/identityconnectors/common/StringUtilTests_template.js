//
// Declare all the graphs..
//
var graphs = [${graphs}];
//
// This method is responsible for pausing the automatic refresh.
//
var paused = false;
function pauseRefresh() {
    var refreshText = "${pause.text}";
    paused = !paused;
    if (paused) {
        refreshText = "${resume.text}";
    }
    $('pause').value = refreshText;
}
//
// This method is responsible for refreshing all the graphs below.
//
function doRefresh(fromButton) {
    // check that this was from a button click
    // not a time where we are paused but the timeout
    // was set to trigger..
    if (fromButton || !paused) {
        $('lastRefreshDate').update(new Date());
        // loop through all graphs calling zoom
        for (var x=0; x<graphs.size(); x++) {
            zoom(x, 0, 100);
        }
        // reset the timer..
        if (!paused) {
            setTimeout("doRefresh(false)", ${refresh.time});
        }
    }
}
//
// This method is responsible for refreshing a particular graph
// and including the proper zooming.
//
function zoom(index, low, high) {
    // refresh the image w/ a new chart based on the low/high percentage.
    Ajax.Updater(
        'graph' + index + '_image',
        '${servlet_mapping}',
        { parameters: $H({ graph_id:graphs[index], low:low, high:high }) }
    );
}
//
// Initialize the page..
//
doRefresh();
