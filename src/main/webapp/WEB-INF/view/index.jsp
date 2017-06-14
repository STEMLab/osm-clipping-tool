<!DOCTYPE html>
<html>
<head>
    <title>OSM Tool Demo</title>
    <meta charset="utf-8">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/static/css/ol.css" type="text/css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/static/css/ol3-layerswitcher.css" type="text/css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/static/css/bootstrap.min.css" type="text/css">
    <script src="${pageContext.request.contextPath}/resources/static/js/ol.js"></script>
    <script src="${pageContext.request.contextPath}/resources/static/js/jquery-3.2.1.min.js"></script>
    <script src="${pageContext.request.contextPath}/resources/static/js/ol3-layerswitcher.js"></script>
    <style>
        .ol-popup {
            position: absolute;
            background-color: white;
            -webkit-filter: drop-shadow(0 1px 4px rgba(0,0,0,0.2));
            filter: drop-shadow(0 1px 4px rgba(0,0,0,0.2));
            padding: 15px;
            border-radius: 10px;
            border: 1px solid #cccccc;
            bottom: 12px;
            left: -50px;
            min-width: 280px;
        }
        .ol-popup:after, .ol-popup:before {
            top: 100%;
            border: solid transparent;
            content: "";
            height: 0;
            width: 0;
            position: absolute;
            pointer-events: none;
        }
        .ol-popup:after {
            border-top-color: white;
            border-width: 10px;
            left: 48px;
            margin-left: -10px;
        }
        .ol-popup:before {
            border-top-color: #cccccc;
            border-width: 11px;
            left: 48px;
            margin-left: -11px;
        }
        .ol-popup-closer {
            text-decoration: none;
            position: absolute;
            top: 2px;
            right: 8px;
        }
        .ol-popup-closer:after {
            content: "x";
        }
    </style>
</head>
<body>
<div id="map" class="map"></div>
<div id="info" class="row" style="color:white;position: absolute;top: 20px;left: 200px;background-color: #7b98bc;">
    <div class="form-inline">
        <span style="margin-left: 10px;">Press ctrl to select range: </span>
    <label class="checkbox">
        <input type="checkbox" id="inlineCheckbox1" value="road" name = "tables" checked> Road
    </label>
    <label class="checkbox">
        <input type="checkbox" id="inlineCheckbox2" name = "tables" value="building"> Building
    </label>
    </div>
</div>
<div id="popup" class="ol-popup">
    <a href="#" id="popup-closer" class="ol-popup-closer"></a>
    <div id="popup-content"></div>
</div>
<script>

    /**
     * Elements that make up the popup.
     */
    var container = document.getElementById('popup');
    var content = document.getElementById('popup-content');
    var closer = document.getElementById('popup-closer');


    /**
     * Create an overlay to anchor the popup to the map.
     */
    var overlay = new ol.Overlay(({
        element: container,
        autoPan: true,
        autoPanAnimation: {
            duration: 250
        }
    }));

    closer.onclick = function() {
        overlay.setPosition(undefined);
        closer.blur();
        return false;
    };

    var wmsSource = new ol.source.ImageWMS({
        url: 'http://localhost:8081/geoserver/osm_demo/wms',
        params: {'LAYERS': 'osm_demo:lakes'}
    });

    var wmsLayer = new ol.layer.Image({
        title:'Lakes',
        visible:false,
        source: wmsSource
    });

    var osm = new ol.layer.Tile({
        title:'OpenStreetMap',
        type: 'base',
        source: new ol.source.OSM()
    });

    var google = new ol.layer.Tile({
        type: 'base',
        title: 'Google',
        visible: false,
        source: new ol.source.TileImage({url: 'http://maps.google.com/maps/vt?pb=!1m5!1m4!1i{z}!2i{x}!3i{y}!4i256!2m3!1e0!2sm!3i375060738!3m9!2spl!3sUS!5e18!12m1!1e47!12m3!1e37!2m1!1ssmartmaps!4e0'})
    })


    var busanLonLat = [76.879196, 43.275867];
    var busantonWebMercator = ol.proj.fromLonLat(busanLonLat);

    var view = new ol.View({
        center: busantonWebMercator,
        zoom: 12
    });

    var vectorSource = new ol.source.Vector({});

    /*var styles = {
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
    };*/
    var styles = {
        'building': new ol.style.Style({
            stroke: new ol.style.Stroke({
                color: 'red',
                lineDash: [4],
                width: 3
            })
        }),
        'road': new ol.style.Style({
            stroke: new ol.style.Stroke({
                color: 'green',
                lineDash: [4],
                width: 3
            })
        })
    };

    var styleFunction = function (feature) {
        return styles[feature.getProperties().tablename];
    };

    var vectorLayer = new ol.layer.Vector({
        title: 'Range query results',
        source: vectorSource,
        style: styleFunction
    });

    var map = new ol.Map({
        layers: [
            new ol.layer.Group({
                title: "Base maps",
                layers:[
                    osm,google
                ]
            }),
            new ol.layer.Group({
                title: 'Features',
                layers:[
                    wmsLayer
                ]
            }),
            vectorLayer
        ],
        overlays: [overlay],
        target: 'map',
        view: view
    });

    // a normal select interaction to handle click
    var select = new ol.interaction.Select();
    map.addInteraction(select);

    // a DragBox interaction used to select features by drawing boxes
    var dragBox = new ol.interaction.DragBox({
        condition: ol.events.condition.platformModifierKeyOnly
    });

    map.addInteraction(dragBox);

    var layerSwitcher = new ol.control.LayerSwitcher({
        tipLabel: 'Switch layer' // Optional label for button
    });
    map.addControl(layerSwitcher);

    dragBox.on('boxend', function () {
        var extent = dragBox.getGeometry().getExtent();
        vectorSource.clear();
        /*addCrossesData(extent);
        addWithinData(extent);*/
        addIntersectsDataData(extent);
    });

    function addIntersectsDataData(extent) {

        var tables = [];

        $("input:checkbox[name=tables]:checked").each(function(){
            tables.push($(this).val());
        });

        if(tables.length==0){
            alert("Select at least one feature!");
            return;
        }

        $.get(
            "intersects",
            {
                xMin: extent[0],
                yMin: extent[1],
                xMax: extent[2],
                yMax: extent[3],
                tables: tables
            },
            function (data) {
                vectorSource.addFeatures((new ol.format.GeoJSON()).readFeatures(data))
            }
        );
    }

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

    var displayFeatureInfo = function(pixel,coordinate) {

        var feature = map.forEachFeatureAtPixel(pixel, function(feature) {
            return feature;
        });
        console.log(feature);
        content.innerHTML = 'You clicked here:';
        overlay.setPosition(coordinate);
    };

    map.on('click', function(evt) {
        displayFeatureInfo(evt.pixel,evt.coordinate);
    });

</script>
</body>
</html>
