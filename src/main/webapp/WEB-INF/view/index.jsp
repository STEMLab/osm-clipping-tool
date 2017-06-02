<!DOCTYPE html>
<html>
<head>
    <title>OSM Tool Demo</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/static/css/ol.css" type="text/css">
    <script src="${pageContext.request.contextPath}/resources/static/js/ol.js"></script>
    <script src="${pageContext.request.contextPath}/resources/static/js/jquery-3.2.1.min.js"></script>
</head>
<body>
<div id="map" class="map"></div>
<div id="info">&nbsp;</div>
<script>


    var wmsSource = new ol.source.ImageWMS({
        url: 'http://localhost:8081/geoserver/osm_demo/wms',
        params: {'LAYERS': 'osm_demo:planet_osm_line'}
    });

    var wmsLayer = new ol.layer.Image({
        source: wmsSource
    });

    var osm = new ol.layer.Tile({
        source: new ol.source.OSM()
    });


    var busanLonLat = [129.080147, 35.233936];
    var busantonWebMercator = ol.proj.fromLonLat(busanLonLat);

    var view = new ol.View({
        center: busantonWebMercator,
        zoom: 16
    });

    var vectorSource = new ol.source.Vector({});

    var styles = {
        'CROSSES': new ol.style.Style({
            stroke: new ol.style.Stroke({
                color: 'red',
                lineDash: [4],
                width: 3
            })
        }),
        'WITHIN': new ol.style.Style({
            stroke: new ol.style.Stroke({
                color: 'green',
                lineDash: [4],
                width: 3
            })
        })
    };

    var styleFunction = function (feature) {
        return styles[feature.getProperties().spatialType];
    };

    var vectorLayer = new ol.layer.Vector({
        source: vectorSource,
        style: styleFunction
    });

    var map = new ol.Map({
        layers: [osm, vectorLayer],
        target: 'map',
        view: view
    });

    // a normal select interaction to handle click
    var select = new ol.interaction.Select();
    map.addInteraction(select);

    // a DragBox interaction used to select features by drawing boxes
    var dragBox = new ol.interaction.DragBox({});

    map.addInteraction(dragBox);

    dragBox.on('boxend', function () {
        var extent = dragBox.getGeometry().getExtent();
        vectorSource.clear();
        addCrossesData(extent);
        addWithinData(extent);
    });

    function addCrossesData(extent) {
        $.get(
            "crosses",
            {
                xMin: extent[0],
                yMin: extent[1],
                xMax: extent[2],
                yMax: extent[3],
            },
            function (data) {
                vectorSource.addFeatures((new ol.format.GeoJSON()).readFeatures(data))
            }
        );
    }

    function addWithinData(extent) {
        $.get(
            "within",
            {
                xMin: extent[0],
                yMin: extent[1],
                xMax: extent[2],
                yMax: extent[3],
            },
            function (data) {
                vectorSource.addFeatures((new ol.format.GeoJSON()).readFeatures(data))
            }
        );
    }

</script>
</body>
</html>
