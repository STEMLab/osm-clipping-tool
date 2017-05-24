<!DOCTYPE html>
<html>
<head>
    <title>OSM Tool Demo</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/static/css/ol.css" type="text/css">
    <script src="${pageContext.request.contextPath}/resources/static/js/ol.js"></script>
</head>
<body>
<div id="map" class="map"></div>
<div id="info">&nbsp;</div>
<script>
    /*var wmsSource = new ol.source.ImageWMS({
     url: 'https://ahocevar.com/geoserver/wms',
     params: {'LAYERS': 'ne:ne'},
     serverType: 'geoserver',
     crossOrigin: 'anonymous'
     });

     var wmsLayer = new ol.layer.Image({
     source: wmsSource
     });*/

    var osm = new ol.layer.Tile({
        source: new ol.source.OSM()
    });

    var busanLonLat = [129.057379, 35.157413];
    var busantonWebMercator = ol.proj.fromLonLat(busanLonLat);

    var view = new ol.View({
        center: busantonWebMercator,
        zoom: 12
    });

    var map = new ol.Map({
        layers: [osm],
        target: 'map',
        view: view
    });

    /*map.on('singleclick', function(evt) {
     document.getElementById('info').innerHTML = '';
     var viewResolution = /!** @type {number} *!/ (view.getResolution());
     var url = wmsSource.getGetFeatureInfoUrl(
     evt.coordinate, viewResolution, 'EPSG:4326',
     {'INFO_FORMAT': 'text/html'});
     if (url) {
     document.getElementById('info').innerHTML =
     '<iframe seamless src="' + url + '"></iframe>';
     }
     });

     map.on('pointermove', function(evt) {
     if (evt.dragging) {
     return;
     }
     var pixel = map.getEventPixel(evt.originalEvent);
     var hit = map.forEachLayerAtPixel(pixel, function() {
     return true;
     });
     map.getTargetElement().style.cursor = hit ? 'pointer' : '';
     });*/
</script>
</body>
</html>
