<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/resources/static/css/font-awesome.min.css"
          type="text/css">
    <script src="${pageContext.request.contextPath}/resources/static/js/ol.js"></script>
    <script src="${pageContext.request.contextPath}/resources/static/js/jquery-3.2.1.min.js"></script>
    <script src="${pageContext.request.contextPath}/resources/static/js/bootstrap.min.js"></script>
    <script src="${pageContext.request.contextPath}/resources/static/js/ol3-layerswitcher.js"></script>
</head>
<body>

<div class="container-fluid">
    <div class="row">
        <div class="col-lg-12">
            <div id="map"></div>
            <div id="popup" class="ol-popup">
                <div id="popup-content"></div>
            </div>
        </div>
    </div>
    <div id="info" class="row">
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
<!-- Modal -->
<div id="modal" class="modal fade" role="dialog" data-toggle="modal">
    <div class="modal-dialog">

        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">Connection to database: </h4>
            </div>
            <div class="modal-body">
                <form action="" id="connectionForm">
                    <table class="table">
                        <tbody>
                        <tr>
                            <td>Host:</td>
                            <c:choose>
                                <c:when test="${empty host}">
                                    <td><input type="text" name="host" value=""></td>
                                </c:when>
                                <c:otherwise>
                                    <td><input type="text" name="host" value="${host}"></td>
                                </c:otherwise>
                            </c:choose>
                        </tr>
                        <tr>
                            <td>Port:</td>
                            <c:choose>
                                <c:when test="${empty port}">
                                    <td><input type="text" name="port" value=""></td>
                                </c:when>
                                <c:otherwise>
                                    <td><input type="text" name="port" value="${port}"></td>
                                </c:otherwise>
                            </c:choose>
                        </tr>
                        <tr>
                            <td>DB name:</td>
                            <c:choose>
                                <c:when test="${empty dbName}">
                                    <td><input type="text" name="name" value=""></td>
                                </c:when>
                                <c:otherwise>
                                    <td><input type="text" name="name" value="${dbName}"></td>
                                </c:otherwise>
                            </c:choose>
                        </tr>
                        <tr>
                            <td>DB user:</td>
                            <c:choose>
                                <c:when test="${empty dbUser}">
                                    <td><input type="text" name="user" value=""></td>
                                </c:when>
                                <c:otherwise>
                                    <td><input type="text" name="user" value="${dbUser}"></td>
                                </c:otherwise>
                            </c:choose>
                        </tr>
                        <tr>
                            <td>Password:</td>
                            <c:choose>
                                <c:when test="${empty dbPassword}">
                                    <td><input type="password" name="password"></td>
                                </c:when>
                                <c:otherwise>
                                    <td><input type="password" name="password" value="${dbPassword}"></td>
                                </c:otherwise>
                            </c:choose>
                        </tr>
                        <tr>
                            <td>
                                <button id="connectButton" type="button" class="btn btn-primary">Connect</button>
                            </td>
                            <c:if test="${isDB eq true}">
                            <td>
                                <span class="alert-success">Connected</span>
                            </td>
                            </c:if>
                        </tbody>
                    </table>
                </form>
            </div>
            <div class="modal-footer">

                <%--// error--%>
                <span class="alert-danger" id="connectionError"></span>
            </div>
        </div>

    </div>
</div>
<div id="selectionModal" class="modal fade" role="dialog" data-toggle="modal">
    <div class="modal-dialog">

        <!-- Modal content-->
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">Choose tables : </h4>
            </div>
            <div class="modal-body" style="overflow-x: auto; max-height: 800px">
                <div class="row">
                    <div class="col-lg-6">
                        <table class="table">
                            <tbody>
                            <tr>
                                <td>Select schema:</td>
                            </tr>
                            <tr>
                                <td>
                                    <select name="" class="selectionSchema" id="first-schema-selection">
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <td>Select table:</td>
                            </tr>
                            <tr>
                                <td>
                                    <select name="" class="selectionTable" disabled id="first-table-selection">
                                    </select>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                        <div class="row">
                            <div class="col-lg-12">
                                <table id="first-result-selection" class="table">
                                    <tbody></tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                    <div class="col-lg-6">
                        <table class="table">
                            <tbody>
                            <tr>
                                <td>Select schema:</td>
                            </tr>
                            <tr>
                                <td>
                                    <select name="" class="selectionSchema" id="second-schema-selection">
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <td>Select table:</td>
                            </tr>
                            <tr>
                                <td>
                                    <select name="" class="selectionTable" disabled id="second-table-selection">
                                    </select>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                        <div class="row">
                            <div class="col-lg-12">
                                <table id="second-result-selection" class="table">
                                    <tbody></tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-lg-12">
                        <h4 style="text-align: center;">Relations</h4>
                        <table class="table table-hover" id="relationResult" style="text-align: center">
                            <tbody>

                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <%--// error--%>
                <span class="alert-danger" id="selectionError"></span>
                <button id="saveButton" type="button" class="btn btn-primary">Edit</button>
            </div>
        </div>

    </div>
</div>
<script>


    /**
     * Elements that make up the popup.
     */
    var container = document.getElementById('popup');
    var content = document.getElementById('popup-content');

    /**
     * Create an overlay to anchor the popup to the map.
     */
    var overlay = new ol.Overlay(/** @type {olx.OverlayOptions} */ ({
        element: container,
        autoPan: true,
        autoPanAnimation: {
            duration: 250
        }
    }));


    var lastExtent;
    var action_list = {
        display_info: true,
        similarity_h: false,
        similarity_s: false,
        add_features: false,
        replace_features: false,
        del_features: false
    };

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

    var googleImagery = new ol.layer.Tile({
        type: 'base',
        title: 'Google Imagery',
        visible: false,
        source: new ol.source.TileImage({url: 'http://khms{s}.google.ru/kh/v=696&x={x}&y={y}&z={z}'})
    })

    var bing_styles = [
        'Road',
        'Aerial',
        'AerialWithLabels',
        'collinsBart',
        'ordnanceSurvey'
    ];
    var bing_layers = [];
    var bi, bii;
    for (bi = 0, bii = bing_styles.length; bi < bii; ++bi) {
        bing_layers.push(new ol.layer.Tile({
            visible: false,
            type: 'base',
            title: bing_styles[bi],
            preload: Infinity,
            source: new ol.source.BingMaps({
                key: 'AhkZpvJJp87JCDamb6ByVF69IlMNUwFVW6NZlzwobneAEa0HS3nNz8sYqTH-BhR0',
                imagerySet: bing_styles[bi]
                // use maxZoom 19 to see stretched tiles instead of the BingMaps
                // "no photos at this zoom level" tiles
                // maxZoom: 19
            })
        }));
    }


    /// set center of map in google's crs
    var busanLonLat = [28.411719, 9.529499];
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
                    new ol.layer.Group({
                        title: 'Bing maps',
                        layers: bing_layers
                    }), google, osm
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
        overlays: [overlay],
        view: view
    });


    function getStyle(feature, matched) {

        if (feature.getProperties().table_type == 'target') {
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
        } else if (feature.getProperties().table_type == 'source') {
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
            //return (feature.getProperties().table_type).toUpperCase()
            return "";
        } else if (map.getView().getZoom() > 16) {
           // return String(feature.getId());
            return "";
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


    var selectSource = new ol.Collection();
    select.on('select', function (evt) {
        evt.selected.forEach(function (feature) {
            selectSource.push(feature);

        });
        evt.deselected.forEach(function (feature) {
            selectSource.remove(feature);
        });
    });

    var modify = new ol.interaction.Modify({
        features: selectSource
    });

    map.addInteraction(modify);

    var originalCoordinates = {};
    modify.on('modifystart', function (evt) {
        evt.features.forEach(function (feature) {
            if (!(feature in originalCoordinates)) {
                originalCoordinates[feature] = feature.getGeometry().getCoordinates();
            }
        });
    });
    modify.on('modifyend', function (evt) {
        evt.features.forEach(function (feature) {
            var coordinate = feature.getGeometry().getExtent();
            var upperCorner = [coordinate[2], coordinate[3], coordinate[0], coordinate[1]];
            content.innerHTML = '<div class="row"><div class="col-md-6" style="text-align: center"><button onclick="saveFeature(' + feature.getId() + ')" class="btn btn-primary">Save</button></div><div class="col-md-6" style="text-align: center"><button onclick="undoFeature(' + feature.getId() + ')" class="btn btn-warning">Undo</button></div></div>';
            overlay.setPosition(upperCorner);
        });
    })

    function saveFeature(id) {
        var feature = select.getFeatures().getArray()[0];
        if (typeof feature != "undefined") {
            if (feature.getProperties().table_type == "target") {
                updateFeature(feature);
            }
            delete originalCoordinates[feature];
        }
        overlay.setPosition(undefined);
    }

    function undoFeature(id) {
        var feature = select.getFeatures().getArray()[0];
        if (feature in originalCoordinates) {
            feature.getGeometry().setCoordinates(
                originalCoordinates[feature]);

            // remove and re-add the feature to make Modify reload it's geometry
            selectSource.remove(feature);
            selectSource.push(feature);
        }
        overlay.setPosition(undefined);
    }

    map.on('singleclick', function (evt) {
        overlay.setPosition(undefined);
    });

    /*custombutton start*/
    var DBbutton = document.createElement('button');
    DBbutton.innerHTML = '<i class="fa fa-database" aria-hidden="true"></i>';

    var handleDBSet = function (e) {
        $('#modal').modal('toggle');
    };

    DBbutton.addEventListener('click', handleDBSet, false);

    var element = document.createElement('div');
    element.className = 'db-set ol-unselectable ol-control';
    element.appendChild(DBbutton);

    var setDBControl = new ol.control.Control({
        element: element
    });
    map.addControl(setDBControl);
    /*custom button end*/

    /*custombutton start*/
    var table_button = document.createElement('button');
    table_button.innerHTML = '<i class="fa fa-table" aria-hidden="true"></i>';

    var handleTableSet = function (e) {
        $('#selectionModal').modal('toggle');
    };

    table_button.addEventListener('click', handleTableSet, false);

    var elementDiv = document.createElement('div');
    elementDiv.className = 'table-set ol-unselectable ol-control';
    elementDiv.appendChild(table_button);

    var setTableControl = new ol.control.Control({
        element: elementDiv
    });
    map.addControl(setTableControl);
    /*custom button end*/

    function addIntersectsDataData(extent) {

        $.get(
            "${pageContext.request.contextPath}/intersects",
            {
                xMin: extent[0],
                yMin: extent[1],
                xMax: extent[2],
                yMax: extent[3]
            },
            function (data) {
                var features = (new ol.format.GeoJSON()).readFeatures(data);
                vectorFeaturesLayer.setVisible(false);
                vectorSource.clear();
                select.getFeatures().clear();
                vectorSource.addFeatures(features);
                matchedData();
            }
        ).fail(function (data) {
            alert(data.responseText);
        });
    }


    function matchedData() {

        divToggle("matchProcessing", true, " &nbsp; Loading...");
        divToggles(["resultContainer"], false);

        $.get(
            "${pageContext.request.contextPath}/processed_intersects",
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
                matchSurfaceHTML += '<tr><th class="col-md-3">ID</th><th class="col-md-3">Box intersection</th><th class="col-md-3">OSM matched feature</th><th class="col-md-3">Surface matching</th></tr>';
                matchhausdorffHTML += '<tr><th class="col-md-3">ID</th><th class="col-md-3">Box intersection</th><th class="col-md-3">OSM matched feature</th><th class="col-md-3">Line matching</th></tr>';
                features.forEach(function (feature) {

                    if (feature.getGeometry().getType() == 'MultiPolygon' || feature.getGeometry().getType() == 'Polygon') {
                        checkPolygon = true;
                        matchSurfaceHTML += '<tr>' + '<td class="col-md-3"><button class="btn btn-link selectFeature" data-feature-id="' + feature.getId() + '">' + feature.getId() + '</button></td>' + '<td class="col-md-3">' + (feature.getProperties().topology_type).toUpperCase() + '</td>' + '<td class="col-md-3"><button class="btn btn-link selectFeature" data-feature-id="' + feature.getProperties().candidate + '">' + feature.getProperties().candidate + '</button></td>' + '<td class="col-md-3">' + parseFloat(Math.round(feature.getProperties().candidateDistance * 100) / 100).toFixed(2) + '%</td>' + '</tr>';
                    } else if (feature.getGeometry().getType() == 'MultiLineString' || feature.getGeometry().getType() == 'LineString') {
                        checkLine = true;
                        matchhausdorffHTML += '<tr>' + '<td class="col-md-3"><button class="btn btn-link selectFeature" data-feature-id="' + feature.getId() + '">' + feature.getId() + '</button></td>' + '<td class="col-md-3">' + (feature.getProperties().topology_type).toUpperCase() + '</td>' + '<td class="col-md-3"><button class="btn btn-link selectFeature" data-feature-id="' + feature.getProperties().candidate + '">' + feature.getProperties().candidate + '</button></td>' + '<td class="col-md-3">' + parseFloat(Math.round(feature.getProperties().candidateDistance * 100) / 100).toFixed(2) + ' %</td>' + '</tr>';
                    } else {
                        console.log("ID: " + feature.getId() + " Source: " + feature.getProperties().table_type + " Topology type: " + feature.getProperties().topology_type + " Name: " + feature.getProperties().name);
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

    function displayAll() {

        $.ajax({
            type: "GET",
            url: "${pageContext.request.contextPath}/features",
            async: true,
            success: function (data) {
                var features = (new ol.format.GeoJSON()).readFeatures(data);
                vectorFeaturesSource.clear();
                vectorFeaturesSource.addFeatures(features);
                var extent = vectorFeaturesLayer.getSource().getExtent();
                map.getView().fit(extent, map.getSize());
            }
        }).fail(function () {
            /*alert("fail loading: " + tables[i]);*/
        });

    }

    $(document).ready(function () {
        <c:if test="${not isDB}">
        $('#modal').modal('show');
        </c:if>
        <c:if test="${isDB eq true}">
        schemasChange();
        uploadRelations();
        displayAll();
        </c:if>
    });

    function uploadRelations() {
        setTimeout(function () {
            <c:if test="${isRelation eq true}">
            <c:forEach items="${relations.relations}" var="rel">
            hashmap.push({id: ++counter, sourceColumn: "${rel.sourceColumn}", targetColumn: "${rel.targetColumn}"});
            $("#relationResult").find('tbody')
                .append($('<tr>').attr('class', 'clickable-row')
                    .append($('<td>')
                        .append($('<span>').attr('data-name-id', counter).attr('data-name-two', "${rel.targetColumn}").html("<code>" + "${rel.sourceColumn}" + "</code> &raquo; <code>" + "${rel.targetColumn}" + "</code>"))
                    )
                );
            </c:forEach>
            </c:if>
        }, 1000);
    }

    select.on('select', function (e) {

        action_list.add_features = false, action_list.similarity_s = false, action_list.display_info = false, action_list.similarity_h = false, action_list.del_features = false, action_list.replace_features = false;

        if (e.target.getFeatures().getLength() == 1) {
            action_list.display_info = true;
            if (e.target.getFeatures().getArray()[0].getProperties().table_type == 'target') {
                action_list.del_features = true;
            } else if (e.target.getFeatures().getArray()[0].getProperties().table_type == 'source') {
                action_list.add_features = true;
            }
        } else if (e.target.getFeatures().getLength() == 2) {
            if (e.target.getFeatures().getArray()[0].getGeometry().getType() == e.target.getFeatures().getArray()[1].getGeometry().getType()) {
                    action_list.replace_features = true;
                    if (e.target.getFeatures().getArray()[0].getGeometry().getType() == 'MultiLineString') {
                        action_list.similarity_h = true;
                    } else if (e.target.getFeatures().getArray()[0].getGeometry().getType() == 'MultiPolygon') {
                        action_list.similarity_s = true;
                    }
            } else {
                //do nothing (for now)
            }
        }
        else if (e.target.getFeatures().getLength() > 2) {
            var onlyOSM = true;
            var onlyKR = true;
            $.each(e.target.getFeatures().getArray(), function (index, value) {
                if (value.getProperties().table_type == 'source') {
                    onlyOSM = false;
                } else if (value.getProperties().table_type == 'target') {
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
            divToggle("action_info", true, "<b>ID:</b> " + features[0].getId() + " <b>Source:</b> " + features[0].getProperties().table_type + " <b>Topology type:</b> " + features[0].getProperties().topology_type + " <b>Name:</b> " + ((features[0].getProperties().name == null) ? 'No information' : features[0].getProperties().name));
        } else if (action_list.similarity_h) {
            getHausdorffDistance(features[0], features[1]);
        } else if (action_list.similarity_s) {
            getSurfaceDistance(features[0], features[1]);
        }

        if (action_list.add_features) {
            $("#action").css("display", "block");
            $("#action").attr("onclick", "addOsmObjects()");
            $("#action").attr("class", "alert btn btn-warning row");
            divToggle("action", true, "Add selected objects to OSM dataset");

        } else if (action_list.replace_features) {
            $("#action").css("display", "block");
            $("#action").attr("onclick", "replaceObjects()");
            $("#action").attr("class", "alert btn btn-warning row");
            divToggle("action", true, "Replace OSM object <b>" + getObject(features, "target").getId() + "</b> with object <b>" + getObject(features, "source").getId() + "</b>");
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

        $.ajax({
            type: "POST",
            url: "${pageContext.request.contextPath}/replace",
            data: JSON.stringify(origArray),
            contentType: "application/json",
            async: false,
            success: function (respose) {
                addIntersectsDataData(lastExtent);
                divToggles(["action", "action_info"], false);
                $("#action_res").attr("class", "alert alert-success row");
                divToggle("action_res", true, "Succesfully replaced");
            }
        }).fail(function () {
            $("#action_res").attr("class", "alert alert-danger row");
            divToggle("action_res", true, "Error on replacing features");
        });
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
            if (features[i].getProperties().table_type == type) {
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
            url: "${pageContext.request.contextPath}/add_to_dataset",
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
            url: "${pageContext.request.contextPath}/hausdorff_distance",
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
            url: "${pageContext.request.contextPath}/surface_distance",
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

    function makeSimple(feature) {
        var simpleObject = {
            type: "Feature",
            id: feature.getId(),
            geometry: {type: feature.getGeometry().getType(), coordinates: feature.getGeometry().getCoordinates()},
            properties: getProperties(feature)
        };
        return simpleObject; // returns cleaned up JSON
    }

    function getProperties(feature) {
        var props = {};
        for (var propertyName in feature.getProperties()) {
            if (propertyName != 'geometry') {
                props[propertyName] = feature.getProperties()[propertyName];
            }
        }
        return props;
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

    $('#connectButton').on('click', function (event) {
        $.ajax({
            type: "POST",
            url: "${pageContext.request.contextPath}/connect",
            data: $("#connectionForm").serializeArray(),
            success: function (respose) {
                $.each(respose, function (key, value) {
                    $('.selectionSchema')
                        .append($("<option></option>")
                            .attr("value", value)
                            .text(value));
                });
                $("#modal").modal("hide");
                $("#selectionModal").modal("show");
            }
        }).fail(function (xhr, status, error) {
            $('#connectionError').html(xhr.responseText);
        });
    });

    function schemasChange() {
        $.ajax({
            type: "GET",
            url: "${pageContext.request.contextPath}/connect/schemas",
            success: function (respose) {
                $.each(respose, function (key, value) {
                    $('.selectionSchema')
                        .append($("<option></option>")
                            .attr("value", value)
                            .text(value));
                });
                $("#first-schema-selection").val("${relations.sourceSchema}").change();
                $("#second-schema-selection").val("${relations.targetSchema}").change();
                onTableChange("first-table-selection");
                onTableChange("second-table-selection");
            }
        }).fail(function (xhr, status, error) {
            $('#connectionError').html(xhr.responseText);
        });
    }

    $(".selectionSchema").on('change', function () {
        cleanRelation();
        onSchemaChange(this.id)
    });

    function onSchemaChange(id) {
        var type;
        if (id == "first-schema-selection") {
            type = "first-table-selection";
        } else if (id == "second-schema-selection") {
            type = "second-table-selection";
        }
        $.ajax({
            type: "GET",
            url: "${pageContext.request.contextPath}/connect/tables",
            data: {'schema': $("#" + id).val()},
            success: function (respose) {
                $('#' + type)
                    .empty();
                $.each(respose, function (key, value) {
                    $('#' + type)
                        .append($("<option></option>")
                            .attr("value", value)
                            .text(value));
                    $("#" + type).prop('disabled', false);
                });
                onTableChange(type);
            }
        }).fail(function (xhr, status, error) {
            $('#connectionError').html(xhr.responseText);
        });
    }


    var countFirst = 0;
    var countSecond = 0;
    var idFirst;
    var idSecond;
    var originKey;
    var originGeom;
    var osmKey;
    var osmGeom;

    $(".selectionTable").on('change', function () {
        cleanRelation();
        onTableChange(this.id);
    });

    function onTableChange(id) {
        var schema;
        var table;
        if (id == "first-table-selection") {
            schema = "first-schema-selection";
            table = "first-result-selection";
        } else if (id == "second-table-selection") {
            schema = "second-schema-selection";
            table = "second-result-selection";
        }
        $.ajax({
            type: "GET",
            url: "${pageContext.request.contextPath}/connect/columns",
            data: {'table': $("#" + id).val(), 'schema': $("#" + schema).val()},
            success: function (respose) {
                $('#' + table).find('tbody')
                    .empty();
                $.each(respose, function (key, value) {
                    if (value.type == 'primary') {
                        if (id == "first-table-selection") {
                            originKey = value.name;
                            $("#" + table).find('tbody')
                                .append($('<tr>')
                                    .append($('<td>')
                                        .append($('<span>').attr('data-name', value.name).html("<code>" + value.name + "</code>: <strong style='color: darkred'>" + value.type + "</strong></code>"))
                                    )
                                );
                        } else if (id == "second-table-selection") {
                            osmKey = value.name;
                            $("#" + table).find('tbody')
                                .append($('<tr>')
                                    .append($('<td>')
                                        .append($('<span>').attr('data-name', value.name).html("<code>" + value.name + "</code>: <strong style='color: darkred'>" + value.type + "</strong></code>"))
                                    )
                                );
                        }
                    }
                    else if (value.type == 'geometry') {
                        if (id == "first-table-selection") {
                            originGeom = value.name;
                            $("#" + table).find('tbody')
                                .append($('<tr>')
                                    .append($('<td>')
                                        .append($('<span>').attr('data-name', value.name).html("<code>" + value.name + "</code>: <strong style='color: darkred'>" + value.type + "</strong></code>"))
                                    )
                                );
                        } else if (id == "second-table-selection") {
                            osmGeom = value.name;
                            $("#" + table).find('tbody')
                                .append($('<tr>')
                                    .append($('<td>')
                                        .append($('<span>').attr('data-name', value.name).html("<code>" + value.name + "</code>: <strong style='color: darkred'>" + value.type + "</strong></code>"))
                                    )
                                );
                        }
                    } else {
                        $("#" + table).find('tbody')
                            .append($('<tr>').attr('class', 'clickable-row')
                                .append($('<td>')
                                    .append($('<span>').attr('data-name', value.name).html("<code>" + value.name + "</code>: <code>" + value.type + ((value.type != 'varchar') ? "" : "</code>(<code>" + value.size + "</code>)")))
                                )
                            );
                    }
                    <c:if test="${not empty relations.sourceTable} and ${not empty relations.targetTable}">
                    $("#first-table-selection").val("${relations.sourceTable}").change();
                    $("#second-table-selection").val("${relations.targetTable}").change();
                    </c:if>
                });
            }
        }).fail(function (xhr, status, error) {
            $('#connectionError').html(xhr.responseText);
        });
    }


    $('#first-result-selection').on('click', '.clickable-row', function (event) {
        if ($(this).hasClass('active')) {
            countFirst = 0;
            $(this).removeClass('active');
        } else {
            countFirst = 1;
            idFirst = $(this).find('span').data('name');
            $(this).addClass('active').siblings().removeClass('active');
        }
        console.log("first " + countFirst);
        check();
    });

    $('#second-result-selection').on('click', '.clickable-row', function (event) {
        if ($(this).hasClass('active')) {
            countSecond = 0;
            $(this).removeClass('active');
        } else {
            countSecond = 1;
            idSecond = $(this).find('span').data('name');
            $(this).addClass('active').siblings().removeClass('active');
        }
        console.log("second " + countSecond)
        check();
    });


    var hashmap = [];
    var counter = 0;
    function check() {
        if (countFirst == 1 && countSecond == 1) {
            hashmap.push({id: ++counter, sourceColumn: idFirst, targetColumn: idSecond});
            $("#relationResult").find('tbody')
                .append($('<tr>').attr('class', 'clickable-row')
                    .append($('<td>')
                        .append($('<span>').attr('data-name-id', counter).attr('data-name-two', idSecond).html("<code>" + idFirst + "</code> &raquo; <code>" + idSecond + "</code>"))
                    )
                );
        }
    }

    $('#relationResult').on('click', '.clickable-row', function (event) {
        var id = $(this).find('span').data('name-id');
        var removeIndex = hashmap.map(function (item) {
            return item.id;
        })
            .indexOf(id);

        ~removeIndex && hashmap.splice(removeIndex, 1);
        $(this).remove();
    });

    $('#saveButton').on('click', function (event) {
        var wrapper = {
            sourceSchema: $("#first-schema-selection").val(),
            targetSchema: $("#second-schema-selection").val(),
            sourceTable: $("#first-table-selection").val(),
            targetTable: $("#second-table-selection").val(),
            relations: hashmap,
            sourceKeyColumn: originKey,
            sourceGeomColumn: originGeom,
            targetKeyColumn: osmKey,
            targetGeomColumn: osmGeom
        };
        $.ajax({
            type: "POST",
            url: "${pageContext.request.contextPath}/connect/relations",
            contentType: 'application/json',
            data: JSON.stringify(wrapper),
            success: function (respose) {
                $("#selectionModal").modal("hide");
                displayAll();
            }
        }).fail(function (xhr, status, error) {
            $('#selectionError').html(xhr.responseText);
        });
    });

    function updateFeature(feature) {
        $.ajax({
            type: "POST",
            url: "${pageContext.request.contextPath}/update_feature",
            contentType: 'application/json',
            data: JSON.stringify(makeSimple(feature)),
            success: function (respose) {
                console.log("updated");
            }
        }).fail(function (xhr, status, error) {
            alert((xhr.responseText));
        });
    }

    function cleanRelation() {
        $("#relationResult > tbody").children().remove();
        hashmap = [];
    }

</script>
</body>
</html>
