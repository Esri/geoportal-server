define([
  'dojo/_base/declare'
], function(declare) {
    return declare(null, {
      queryUrl: null,
      start:1,
      max:10,
      f: 'json',
      contentType: null,
      searchText: null,
      spatialRel: 'esriSpatialRelOverlaps',
      bbox : null
   });
});