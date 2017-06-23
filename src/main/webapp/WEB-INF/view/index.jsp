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
        <ul class="sidebar-nav ">
            <li>
                <h3>KR:</h3>
            </li>
            <li>
                <div id="kr_res"></div>
            </li>
            <li>
            <li>
                <h3>OSM:</h3>
            </li>
            <li>
                <div id="osm_res"></div>
            </li>
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
    var lastExtent;

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
    var selectedVectorSource = new ol.source.Vector({});

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

    var selectedStyleFunction = function (feature) {
        return new ol.style.Style({
            stroke: new ol.style.Stroke({
                color: 'blue',
                width: 5
            }),
            fill: new ol.style.Fill({
                color: 'blue'
            })
        })
    };

    var vectorLayer = new ol.layer.Vector({
        title: 'Range query results',
        source: vectorSource,
        style: styleFunction
    });

    var selectedVectorLayer = new ol.layer.Vector({
        title: 'Selected layer',
        source: selectedVectorSource,
        style: selectedStyleFunction
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
            vectorLayer, selectedVectorLayer
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
        lastExtent = extent;
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

                var osmHtml = '<table id="osm_table" class="table table-condensed">';
                var krHtml = '<table id="kr_table" class="table table-condensed">';
                osmHtml += '<tr><th class="col-md-3">ID</th><th class="col-md-3">Topology</th><th class="col-md-4">Name</th><th class="col-md-2">Add to KR</th></th>';
                krHtml += '<tr><th class="col-md-3">ID</th><th class="col-md-3">Topology</th><th class="col-md-6">Name</th></th>';
                features.forEach(function (feature) {
                    if(feature.getProperties().source=="osm"){
                        osmHtml += '<tr data-feature-id="'+feature.getId()+'">'+'<td class="col-md-3">' + feature.getId() + '</td>'+'<td class="col-md-3">' +  feature.getProperties().topology_type + '</td>'+'<td class="col-md-4">'+  feature.getProperties().name + '</td>'+'<td class="col-md-2">'+ '<a href="#" class="addOsm" data-feature-id="'+feature.getId()+'"> +add</a>'+ '</td>'+'</tr>';
                    }else if(feature.getProperties().source=="kr"){
                        krHtml += '<tr data-feature-id="'+feature.getId()+'">'+'<td class="col-md-3">' + feature.getId() + '</td>'+'<td class="col-md-3">' +  feature.getProperties().topology_type + '</td>'+'<td class="col-md-6">'+  feature.getProperties().name + '</td>'+'</tr>';
                    }else{
                        console.log("ID: " + feature.getId() + " Source: " + feature.getProperties().source + " Topology type: " + feature.getProperties().topology_type + " Name: " + feature.getProperties().name);
                    }
                });
                osmHtml += '</table>';
                krHtml += '</table>';
                $("#osm_res").empty();
                $("#kr_res").empty();
                $("#osm_res").append(osmHtml);
                $("#kr_res").append(krHtml);

                $('#osm_table').on('click', 'tbody tr', function(event) {
                    $(this).addClass('highlight').siblings().removeClass('highlight');
                    $(this).css('color','white').siblings().css('color','#7b98bc');

                     unSelect("kr_table");
                    var feature = vectorSource.getFeatureById($(this).attr("data-feature-id"));
                    map.getView().fit(feature.getGeometry(), map.getSize());
                    console.log();
                    selectedVectorSource.clear();
                    selectedVectorSource.addFeature(feature);
                });

                $('#kr_table').on('click', 'tbody tr', function(event) {
                    $(this).addClass('highlight').siblings().removeClass('highlight');
                    $(this).css('color','white').siblings().css('color','#7b98bc');

                    unSelect("osm_table");

                    var feature = vectorSource.getFeatureById($(this).attr("data-feature-id"));
                    map.getView().fit(feature.getGeometry(), map.getSize());
                    console.log();
                    selectedVectorSource.clear();
                    selectedVectorSource.addFeature(feature);
                });

                $(".addOsm").click(function(e) {
                    e.preventDefault();
                    var feature = vectorSource.getFeatureById($(this).attr("data-feature-id"));
                    $.ajax({
                        type: "POST",
                        url: "${pageContext.request.contextPath}/addOsmToDataset",
                        data: simpleStringify(feature),
                        contentType: "application/json",
                        async: false,
                        success: function (respose) {
                            console.log("Success adding");
                            addIntersectsDataData(extent);
                        }
                    }).fail(function() {
                        alert( "Error adding" );
                    });
                });

                $("#wrapper").addClass("toggled");
            }
        );
    }

    function unSelect(table){
        $('#'+table+' > tbody  > tr').each(function() {$(this).removeClass('highlight'); $(this).css('color','#7b98bc');});
    }



    var displayFeatureInfo = function (pixel, coordinate) {

        var feature = map.forEachFeatureAtPixel(pixel, function (feature) {
            return feature;
        });
        console.log(feature);
        content.innerHTML = 'ID: ' + feature.getId() + ' </br> Description:' + feature.getProperties().name;
        overlay.setPosition(coordinate);

        selectedVectorSource.addFeature(feature);
        if(selectedVectorSource.getFeatures().length==2){
            if(selectedVectorSource.getFeatures()[0].getGeometry().getType()==selectedVectorSource.getFeatures()[1].getGeometry().getType()){
                if(selectedVectorSource.getFeatures()[0].getProperties().tablename!=selectedVectorSource.getFeatures()[1].getProperties().tablename){
                   if(selectedVectorSource.getFeatures()[0].getProperties().tablename=='building' || selectedVectorSource.getFeatures()[0].getProperties().tablename=='building_kr'){
                       getSurfaceDistance(selectedVectorSource.getFeatures()[0], selectedVectorSource.getFeatures()[1]);
                   } else{
                       getHausdorffDistance(selectedVectorSource.getFeatures()[0], selectedVectorSource.getFeatures()[1]);
                   }
                }
            }else{
                selectedVectorSource.clear();
            }
        }  if(selectedVectorSource.getFeatures().length>=3){
            selectedVectorSource.clear();
        }
    };

    function getHausdorffDistance(one, two){
        $.ajax({
            type: "POST",
            url: "${pageContext.request.contextPath}/hausdorffDistance",
            data: simpleStringify(one,two),
            contentType: "application/json",
            async: false,
            success: function (respose) {
                console.log("Hausdorf distance" + respose);
            }
        }).fail(function() {
            alert( "Error hausdorff" );
        });
    }
    function getSurfaceDistance(one, two){
        $.ajax({
            type: "POST",
            url: "${pageContext.request.contextPath}/surfaceDistance",
            data: simpleStringify(one,two),
            contentType: "application/json",
            async: false,
            success: function (respose) {
                console.log("Surface distance" + respose);
            }
        }).fail(function() {
            alert( "Error surfaceDistance" );
        });
    }

    map.on('click', function (evt) {
        $("#wrapper").removeClass("toggled");
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
        $("#wrapper").removeClass("toggled");
    });

    $(document).keydown(function(event){
        if(event.which=="17"){
            $("#wrapper").removeClass("toggled");
        }
    });

    function simpleStringify (feature){
        var simpleObject = {id:feature.getId(), properties : {'tablename': feature.getProperties().tablename}};
        return JSON.stringify(simpleObject); // returns cleaned up JSON
    };

    function simpleStringify (one, two){
        var simpleObject = {id:one.getId(), properties : {'tablename': one.getProperties().tablename}};
        var simpleObject1 = {id:two.getId(), properties : {'tablename': two.getProperties().tablename}};
        var arr = [simpleObject,simpleObject1];
        return JSON.stringify(arr); // returns cleaned up JSON
    };

</script>
</body>
</html>
