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

<div class="container-fluid">
    <div class="row">
        <div class="col-lg-12">
            <div id="map"></div>
        </div>
    </div>
    <div id="info" class="row">
        <%--<div class="form-inline">
            <span style="margin-left: 10px;">WMS: </span>
            <label class="checkbox">
                <input type="checkbox" id="inlineCheckbox1" value="roadl_50k" name="tables"> Road 50K
            </label>
            &lt;%&ndash;<label class="checkbox">
                <input type="checkbox" id="inlineCheckbox2" name="tables" value="building"> Building OSM
            </label>&ndash;%&gt;
            <label class="checkbox">
                <input type="checkbox" id="inlineCheckbox3" value="roadl_urban" name="tables"> Road Urban
            </label>
            <label class="checkbox">
                <input type="checkbox" id="inlineCheckbox4" name="tables" value="road_sa"> Road OSM
            </label>
        </div>--%>
    </div>
    <div id="matchResult">
        <div id="matchProcessing">
            <div>
                <span>&nbsp;To select area by drawing boxes press <code>Shift &#8679; + click</code></span></br>
                <span>&nbsp;To select multiple features hold <code>Shift &#8679;</code></span></br>
                <span>&nbsp;To refresh page press <code>Ctrl + R</code></span>
            </div>
        </div>
        <div id="resultContainer" class="container-fluid">
            <div id="matchSurface" class="row">
                <div class="col-lg-12"></div>
            </div>
            <div id="matchHausdorff" class="row">
                <div class="col-lg-12"></div>
            </div>
        </div>
    </div>
    <div id="actionDiv">
        <div class="alert alert-info row" id="action_info"></div>
        <div class="alert btn btn-warning row" id="action"></div>
        <div class="alert alert-success row" id="action_res"></div>
    </div>
</div>
<script>

    var lastExtent;
    var action_list = {
        display_info: true,
        similarity_h: false,
        similarity_s: false,
        add_features: false,
        replace_features: false,
        del_features: false
    };

    /*var wmsSource = new ol.source.ImageWMS({
        url: 'http://localhost:8081/geoserver/osm_demo/wms',
        params: {'LAYERS': ''}
    });*/

   /* var wmsLayer = new ol.layer.Image({
        title: 'WMS',
        visible: true,
        source: wmsSource
    });*/

    var osm = new ol.layer.Tile({
        title: 'OpenStreetMap',
        type: 'base',
        visible: true,
        source: new ol.source.OSM()
    });

    var google = new ol.layer.Tile({
        type: 'base',
        title: 'Google',
        visible: false,
        source: new ol.source.TileImage({url: 'http://maps.google.com/maps/vt?pb=!1m5!1m4!1i{z}!2i{x}!3i{y}!4i256!2m3!1e0!2sm!3i375060738!3m9!2spl!3sUS!5e18!12m1!1e47!12m3!1e37!2m1!1ssmartmaps!4e0'})
    })


    /// set center of map in google's crs
     var busanLonLat = [28.411719, 9.529499 ];
     var busantonWebMercator = ol.proj.fromLonLat(busanLonLat);

    var view = new ol.View({
        center: busantonWebMercator,
        zoom: 10
    });

    var vectorSource = new ol.source.Vector({});

    var vectorFeaturesSource = new ol.source.Vector({});

    var styleFunction = function (feature) {
        return getStyle(feature);
    };
    var vectorLayer = new ol.layer.Vector({
        source: vectorSource,
        style: styleFunction
    });

    var vectorFeaturesLayer = new ol.layer.Vector({
        source: vectorFeaturesSource,
        visible: false,
        title: 'All data',
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
                    //wmsLayer
                    vectorFeaturesLayer
                ]
            }),
            vectorLayer
        ],
        target: 'map',
        view: view
    });

    function getStyle(feature, matched) {

        if (feature.getProperties().source == 'osm') {
            if (feature.getProperties().candidate != null) {
                return new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: 'red',
                        lineDash: [4],
                        width: 3
                    }),
                    text: new ol.style.Text({
                        font: '12px Calibri,sans-serif',
                        fill: new ol.style.Fill({color: '#000'}),
                        stroke: new ol.style.Stroke({
                            color: 'red', width: 2
                        }),
                        // get the text from the feature - `this` is ol.Feature
                        // and show only under certain resolution
                        text: getText(feature)
                    })
                })
            } else {
                return new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: 'red',
                        width: 3
                    }),
                    text: new ol.style.Text({
                        font: '12px Calibri,sans-serif',
                        fill: new ol.style.Fill({color: '#000'}),
                        stroke: new ol.style.Stroke({
                            color: 'red', width: 2
                        }),
                        // get the text from the feature - `this` is ol.Feature
                        // and show only under certain resolution
                        text: getText(feature)
                    })
                })
            }
        } else if (feature.getProperties().source == 'un') {
            if (feature.getProperties().candidate != null) {
                return new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: 'black',
                        lineDash: [4],
                        width: 3
                    }),
                    text: new ol.style.Text({
                        font: '12px Calibri,sans-serif',
                        fill: new ol.style.Fill({color: '#000'}),
                        stroke: new ol.style.Stroke({
                            color: 'black', width: 2
                        }),
                        // get the text from the feature - `this` is ol.Feature
                        // and show only under certain resolution
                        text: getText(feature)
                    })
                })
            } else {
                return new ol.style.Style({
                    stroke: new ol.style.Stroke({
                        color: 'black',
                        width: 3
                    }),
                    text: new ol.style.Text({
                        font: '12px Calibri,sans-serif',
                        fill: new ol.style.Fill({color: '#000'}),
                        stroke: new ol.style.Stroke({
                            color: 'black', width: 2
                        }),
                        // get the text from the feature - `this` is ol.Feature
                        // and show only under certain resolution
                        text: getText(feature)
                    })
                })
            }
        }
    }

    function getText(feature) {

        if (map.getView().getZoom() > 15 && map.getView().getZoom() <= 16) {
            return (feature.getProperties().source).toUpperCase()
        } else if (map.getView().getZoom() > 16) {
            return String(feature.getId());
        }
        else {
            return "";
        }
    }


    // a normal select interaction to handle click
    var select = new ol.interaction.Select({});
    map.addInteraction(select);

    // a DragBox interaction used to select features by drawing boxes
    var dragBox = new ol.interaction.DragBox({
        condition: ol.events.condition.shiftKeyOnly
    });

    map.addInteraction(dragBox);

    var layerSwitcher = new ol.control.LayerSwitcher({
        tipLabel: 'Switch layer'
    });
    map.addControl(layerSwitcher);

    dragBox.on('boxend', function () {
        var extent = dragBox.getGeometry().getExtent();
        divToggles(["action_info", "action_res", "action"], false);
        addIntersectsDataData(extent);
        lastExtent = extent;
    });

    function addIntersectsDataData(extent) {


        var tables = ["road_sa", "roadl_50k", "roadl_urban"];

        $.get(
            "${pageContext.request.contextPath}/intersects",
            {
                xMin: extent[0],
                yMin: extent[1],
                xMax: extent[2],
                yMax: extent[3],
                tables: tables
            },
            function (data) {
                var features = (new ol.format.GeoJSON()).readFeatures(data);
                vectorFeaturesLayer.setVisible(false);
                vectorSource.clear();
                select.getFeatures().clear();
                vectorSource.addFeatures(features);
                matchedData();
            }
        );
    }


    function matchedData() {

        divToggle("matchProcessing", true, " &nbsp; Loading...");
        divToggles(["resultContainer"], false);

        $.get(
            "${pageContext.request.contextPath}/intersectsProcess",
            function (data) {

                divToggles(["matchProcessing"], false);
                divToggles(["resultContainer"], true);

                var features = (new ol.format.GeoJSON()).readFeatures(data);

                if (features.length == 0) {
                    divToggles(["resultContainer"], false);
                    divToggle("matchProcessing", true, " &nbsp; No matching data...");
                }

                var checkPolygon = false;
                var checkLine = false;

                var matchSurfaceHTML = '<table class="table table-responsive">';
                var matchhausdorffHTML = '<table class="table table-responsive">';
                matchSurfaceHTML += '<tr><th class="col-md-2">ID</th><th class="col-md-2">Box intersection</th><th class="col-md-3">Name</th><th class="col-md-3">OSM matched feature</th><th class="col-md-2">Surface matching</th></tr>';
                matchhausdorffHTML += '<tr><th class="col-md-2">ID</th><th class="col-md-2">Box intersection</th><th class="col-md-3">Name</th><th class="col-md-3">OSM matched feature</th><th class="col-md-2">Line matching</th></tr>';
                features.forEach(function (feature) {

                    if (feature.getGeometry().getType() == 'MultiPolygon') {
                        checkPolygon = true;
                        matchSurfaceHTML += '<tr>' + '<td class="col-md-2"><button class="btn btn-link selectFeature" data-feature-id="' + feature.getId() + '">' + feature.getId() + '</button></td>' + '<td class="col-md-2">' + (feature.getProperties().topology_type).toUpperCase() + '</td>' + '<td class="col-md-3">' + ((feature.getProperties().name == null) ? 'No information' : feature.getProperties().name) + '</td>' + '<td class="col-md-3"><button class="btn btn-link selectFeature" data-feature-id="' + feature.getProperties().candidate + '">' + feature.getProperties().candidate + '</button></td>' + '<td class="col-md-2">' + parseFloat(Math.round(feature.getProperties().candidateDistance * 100) / 100).toFixed(2) + '%</td>' + '</tr>';
                    } else if (feature.getGeometry().getType() == 'MultiLineString') {
                        checkLine = true;
                        matchhausdorffHTML += '<tr>' + '<td class="col-md-2"><button class="btn btn-link selectFeature" data-feature-id="' + feature.getId() + '">' + feature.getId() + '</button></td>' + '<td class="col-md-2">' + (feature.getProperties().topology_type).toUpperCase() + '</td>' + '<td class="col-md-3">' + ((feature.getProperties().name == null) ? 'No information' : feature.getProperties().name) + '</td>' + '<td class="col-md-3"><button class="btn btn-link selectFeature" data-feature-id="' + feature.getProperties().candidate + '">' + feature.getProperties().candidate + '</button></td>' + '<td class="col-md-2">' + parseFloat(Math.round(feature.getProperties().candidateDistance * 100) / 100).toFixed(2) + ' %</td>' + '</tr>';
                    } else {
                        console.log("ID: " + feature.getId() + " Source: " + feature.getProperties().source + " Topology type: " + feature.getProperties().topology_type + " Name: " + feature.getProperties().name);
                    }
                    vectorSource.removeFeature(vectorSource.getFeatureById(feature.getId()));
                    vectorSource.addFeature(feature);

                });
                matchSurfaceHTML += '</table>';
                matchhausdorffHTML += '</table>';
                $("#matchSurface").empty();
                $("#matchHausdorff").empty();

                if (checkPolygon) {
                    $("#matchSurface").append(matchSurfaceHTML)
                }
                if (checkLine) {
                    $("#matchHausdorff").append(matchhausdorffHTML)
                }

                $('.selectFeature').on('click', function (event) {

                    var feature = vectorSource.getFeatureById($(this).attr("data-feature-id"));
                    map.getView().fit(feature.getGeometry());

                    select.getFeatures().clear();
                    select.getFeatures().push(feature);

                });
            }
        ).fail(function () {
            $("#matchProcessing").html(" &nbsp; Fail processing...");
        })


    }

    function displayAll(){
        var tables = ["road_sa", "roadl_50k", "roadl_urban"];
        for(var i=0; i<tables.length; i++){
            $.ajax({
                type: "GET",
                url: "${pageContext.request.contextPath}/features",
                data: {"table": tables[i]},
                async: true,
                success: function (data) {
                    var features = (new ol.format.GeoJSON()).readFeatures(data);
                    vectorFeaturesSource.addFeatures(features);
                }
            }).fail(function () {
                /*alert("fail loading: " + tables[i]);*/
            });
        }
    }

    $( document ).ready(function() {
        displayAll();
    });

    select.on('select', function (e) {

        action_list.add_features = false, action_list.similarity_s = false, action_list.display_info = false, action_list.similarity_h = false, action_list.del_features = false, action_list.replace_features = false;

        if (e.target.getFeatures().getLength() == 1) {
            action_list.display_info = true;
            if (e.target.getFeatures().getArray()[0].getProperties().source == 'osm') {
                action_list.del_features = true;
            } else if (e.target.getFeatures().getArray()[0].getProperties().source == 'un') {
                action_list.add_features = true;
            }
        } else if (e.target.getFeatures().getLength() == 2) {
            if (e.target.getFeatures().getArray()[0].getGeometry().getType() == e.target.getFeatures().getArray()[1].getGeometry().getType()) {
                if (e.target.getFeatures().getArray()[0].getProperties().tablename != e.target.getFeatures().getArray()[1].getProperties().tablename) {
                    action_list.replace_features = true;
                    if (e.target.getFeatures().getArray()[0].getGeometry().getType() == 'MultiLineString') {
                        action_list.similarity_h = true;
                    } else if (e.target.getFeatures().getArray()[0].getGeometry().getType() == 'MultiPolygon') {
                        action_list.similarity_s = true;
                    }
                } else {
                    if (e.target.getFeatures().getArray()[0].getProperties().source == 'osm' && e.target.getFeatures().getArray()[1].getProperties().source == 'osm') {
                        action_list.del_features = true;
                    }
                    else if (e.target.getFeatures().getArray()[0].getProperties().source == 'un' && e.target.getFeatures().getArray()[1].getProperties().source == 'un') {
                        action_list.add_features = true;
                    }
                }
            } else {
                //do nothing (for now)
            }
        }
        else if (e.target.getFeatures().getLength() > 2) {
            var onlyOSM = true;
            var onlyKR = true;
            $.each(e.target.getFeatures().getArray(), function (index, value) {
                if (value.getProperties().source == 'un') {
                    onlyOSM = false;
                } else if (value.getProperties().source == 'osm') {
                    onlyKR = false;
                }
            });

            if (onlyOSM) action_list.del_features = true;
            if (onlyKR) action_list.add_features = true;
        }
        processActionList();
    });

    function processActionList() {
        var features = select.getFeatures().getArray();

        divToggles(["action", "action_info", "action_res"], false);

        if (action_list.display_info) {
            divToggle("action_info", true, "<b>ID:</b> " + features[0].getId() + " <b>Source:</b> " + features[0].getProperties().source + " <b>Topology type:</b> " + features[0].getProperties().topology_type + " <b>Name:</b> " + ((features[0].getProperties().name == null) ? 'No information' : features[0].getProperties().name));
        } else if (action_list.similarity_h) {
            getHausdorffDistance(features[0], features[1]);
        } else if (action_list.similarity_s) {
            getSurfaceDistance(features[0], features[1]);
        }

        if (action_list.add_features) {
            $("#action").css("display", "block");
            $("#action").attr("onclick", "addOsmObjects()");
            $("#action").attr("class", "alert btn btn-warning row");
            divToggle("action", true, "Add selected UN objects to OSM dataset");

        } else if (action_list.replace_features) {
            $("#action").css("display", "block");
            $("#action").attr("onclick", "replaceObjects()");
            $("#action").attr("class", "alert btn btn-warning row");
            divToggle("action", true, "Replace OSM object <b>" + getObject(features, "osm").getId() + "</b> with UN object <b>" + getObject(features, "un").getId() + "</b>");
        } else if (action_list.del_features) {
            $("#action").css("display", "block");
            $("#action").attr("onclick", "deleteObjects()");
            $("#action").attr("class", "alert btn btn-danger row");
            divToggle("action", true, "Delete selected OSM objects");
        }

    }

    function replaceObjects() {
        var origArray = [];

        $.each(select.getFeatures().getArray(), function (index, value) {
            origArray.push(makeSimple(value));
        });

        f
    }

    function deleteObjects() {
        var origArray = [];

        $.each(select.getFeatures().getArray(), function (index, value) {
            origArray.push(makeSimple(value));
        });

        $.ajax({
            type: "POST",
            url: "${pageContext.request.contextPath}/delete",
            data: JSON.stringify(origArray),
            contentType: "application/json",
            async: false,
            success: function (respose) {
                addIntersectsDataData(lastExtent);
                divToggles(["action", "action_info"], false);
                $("#action_res").attr("class", "alert alert-success row");
                divToggle("action_res", true, "Succesfully deleted");
            }
        }).fail(function () {
            $("#action_res").attr("class", "alert alert-danger row");
            divToggle("action_res", true, "Error on deleting features");
        });
    }

    function getObject(features, type) {
        for (var i = 0; i < features.length; i++) {
            if (features[i].getProperties().source == type) {
                return features[i];
            }
        }
    }

    function addOsmObjects() {

        var origArray = [];

        $.each(select.getFeatures().getArray(), function (index, value) {
            origArray.push(makeSimple(value));
        });

        $.ajax({
            type: "POST",
            url: "${pageContext.request.contextPath}/addToOsmDataSet",
            data: JSON.stringify(origArray),
            contentType: "application/json",
            async: false,
            success: function (respose) {
                addIntersectsDataData(lastExtent);
                divToggles(["action", "action_info"], false);
                $("#action_res").attr("class", "alert alert-success row");
                divToggle("action_res", true, "Succesfully added to OSM dataset");
            }
        }).fail(function () {
            $("#action_res").attr("class", "alert alert-danger row");
            divToggle("action_res", true, "Error on adding to OSM dataset");
        });
    }

    function getHausdorffDistance(one, two) {

        var arr = [makeSimple(one), makeSimple(two)];

        $.ajax({
            type: "POST",
            url: "${pageContext.request.contextPath}/hausdorffDistance",
            data: JSON.stringify(arr),
            contentType: "application/json",
            async: false,
            success: function (respose) {
                divToggle("action_info", true, "Hausdorff matching: " + parseFloat(Math.round(respose * 100) / 100).toFixed(2) + " %")
            }
        }).fail(function () {
            alert("Error hausdorff");
        });
    }

    function getSurfaceDistance(one, two) {

        var arr = [makeSimple(one), makeSimple(two)];

        $.ajax({
            type: "POST",
            url: "${pageContext.request.contextPath}/surfaceDistance",
            data: JSON.stringify(arr),
            contentType: "application/json",
            async: false,
            success: function (respose) {
                divToggle("action_info", true, "Surface Matching: " + parseFloat(Math.round(respose * 100) / 100).toFixed(1) + "%");
            }
        }).fail(function () {
            alert("Error surfaceDistance");
        });
    }

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

    function makeSimple(feature) {
        var simpleObject = {
            type: "Feature",
            id: feature.getId(),
            geometry: {type: feature.getGeometry().getType(), coordinates: feature.getGeometry().getCoordinates()},
            properties: {
                'tablename': feature.getProperties().tablename,
                'topology_type': feature.getProperties().topology_type,
                'name': feature.getProperties().name,
                'source': feature.getProperties().source
            }
        };
        return simpleObject; // returns cleaned up JSON
    }

    function divToggles(id, show) {
        $.each(id, function (index, value) {
            if (show) {
                $("#" + value).show();
            } else {
                $("#" + value).hide();
            }
        });
    }

    function divToggle(id, show, data) {
        $("#" + id).html(data);
        if (show) {
            $("#" + id).show();
        } else {
            $("#" + id).hide();
        }
    }


</script>
</body>
</html>
