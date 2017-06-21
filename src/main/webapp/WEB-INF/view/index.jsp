<!DOCTYPE html>
<html>
<head>
    <title>OSM Tool Demo</title>
    <meta charset="utf-8">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/static/css/ol.css" type="text/css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/static/css/ol3-layerswitcher.css"
          type="text/css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/static/css/bootstrap.min.css"
          type="text/css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/static/css/style.css"
          type="text/css">
    <script src="${pageContext.request.contextPath}/resources/static/js/ol.js"></script>
    <script src="${pageContext.request.contextPath}/resources/static/js/jquery-3.2.1.min.js"></script>
    <script src="${pageContext.request.contextPath}/resources/static/js/ol3-layerswitcher.js"></script>
</head>
<body>

<div id="wrapper">
    <!-- Sidebar -->
    <div id="sidebar-wrapper">
        <ul class="sidebar-nav">
            <li>
                <h3>OSM data:</h3>
            </li>
            <li>
                <div id="osm_res"></div>
            </li>
            <li>
                <h3>KR data:</h3>
            </li>
            <li>
                <div id="kr_res"></div>
            </li>
            <li>
                <a href="#" id="bar_close">Close bar</a>
            </li>

        </ul>
    </div>
    <!-- /#sidebar-wrapper -->

    <!-- Page Content -->
    <div id="page-content-wrapper">
        <div class="container-fluid">
            <div class="row">
                <div class="col-lg-12">
                    <div id="map"></div>
                    <div id="info" class="row">
                        <div class="form-inline">
                            <span style="margin-left: 10px;">WMS: </span>
                            <label class="checkbox">
                                <input type="checkbox" id="inlineCheckbox1" value="road" name="tables"> Road OSM
                            </label>
                            <label class="checkbox">
                                <input type="checkbox" id="inlineCheckbox2" name="tables" value="building"> Building OSM
                            </label>
                            <label class="checkbox">
                                <input type="checkbox" id="inlineCheckbox3" value="road_kr" name="tables"> Road KR
                            </label>
                            <label class="checkbox">
                                <input type="checkbox" id="inlineCheckbox4" name="tables" value="building_kr"> Building
                                KR
                            </label>
                        </div>
                    </div>
                    <div id="popup" class="ol-popup">
                        <a href="#" id="popup-closer" class="ol-popup-closer"></a>
                        <div id="popup-content"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <!-- /#page-content-wrapper -->

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

    closer.onclick = function () {
        overlay.setPosition(undefined);
        closer.blur();
        return false;
    };

    var wmsSource = new ol.source.ImageWMS({
        url: 'http://localhost:8081/geoserver/osm_demo/wms',
        params: {'LAYERS': ''}
    });

    var wmsLayer = new ol.layer.Image({
        title: 'WMS',
        visible: true,
        source: wmsSource
    });

    var osm = new ol.layer.Tile({
        title: 'OpenStreetMap',
        type: 'base',
        source: new ol.source.OSM()
    });

    var google = new ol.layer.Tile({
        type: 'base',
        title: 'Google',
        visible: false,
        source: new ol.source.TileImage({url: 'http://maps.google.com/maps/vt?pb=!1m5!1m4!1i{z}!2i{x}!3i{y}!4i256!2m3!1e0!2sm!3i375060738!3m9!2spl!3sUS!5e18!12m1!1e47!12m3!1e37!2m1!1ssmartmaps!4e0'})
    })


    /* var busanLonLat = [];
     var busantonWebMercator = ol.proj.fromLonLat(busanLonLat);*/

    var view = new ol.View({
        center: [14129544.82809238, 4517183.023123029],
        zoom: 15
    });

    var vectorSource = new ol.source.Vector({});

    var styles = {
        'osm': new ol.style.Style({
            stroke: new ol.style.Stroke({
                color: 'red',
                lineDash: [4],
                width: 3
            })
        }),
        'kr': new ol.style.Style({
            stroke: new ol.style.Stroke({
                color: 'black',
                lineDash: [4],
                width: 3
            })
        })
    };

    var styleFunction = function (feature) {
        return styles[feature.getProperties().source];
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
                layers: [
                    google, osm
                ]
            }),
            new ol.layer.Group({
                title: 'Features',
                layers: [
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
        addIntersectsDataData(extent);
    });

    function addIntersectsDataData(extent) {

        var tables = ["building", "road", "road_kr", "building_kr"];

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
                var features = (new ol.format.GeoJSON()).readFeatures(data);
                vectorSource.addFeatures(features);

                features.forEach(function (feature) {
                    console.log("ID: " + feature.getId() + " Source: " + feature.getProperties().source + " Topology type: " + feature.getProperties().topology_type + " Name: " + feature.getProperties().name);
                });

                $("#wrapper").toggleClass("toggled");
            }
        );
    }

    var displayFeatureInfo = function (pixel, coordinate) {

        var feature = map.forEachFeatureAtPixel(pixel, function (feature) {
            return feature;
        });
        console.log(feature);
        content.innerHTML = 'Source: ' + feature.getProperties().source + ' </br> Description:' + feature.getProperties().name;
        overlay.setPosition(coordinate);
    };

    map.on('click', function (evt) {
        displayFeatureInfo(evt.pixel, evt.coordinate);
    });

    $("input:checkbox[name=tables]").change(function () {
        var tables = [];
        $("input:checkbox[name=tables]:checked").each(function () {
            tables.push("osm_demo:" + $(this).val());
        });
        if (tables.length == 0) {
            wmsLayer.setVisible(false);
        } else {
            wmsLayer.setVisible(true);
        }
        var params = wmsSource.getParams();
        params.LAYERS = tables.join();
        wmsSource.updateParams(params);
        wmsSource.refresh();
    });

    $("#bar_close").click(function (e) {
        e.preventDefault();
        $("#wrapper").toggleClass("toggled");
    });

</script>
</body>
</html>
