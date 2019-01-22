define({
  root: ({
    search: {
      featureLayerTitlePattern: "{serviceName} - {layerName}",
      layerInaccessible: "The layer is inaccessible.",
      loadError: "GeoportalSearch, unable to load:",
      targetOptions: {
        caption: "Catalog",
        enabled: "Enabled"
      },
      searchBox: {
        search: "Search",
        placeholder: "Search..."
      },
      bboxOption: {
        bbox: "BBox"
      },
      liveDataOption: {
        liveData: "Live Data"
      },
      sortOptions: {
        prompt: "Sort",
        relevance: "Relevance",
        title: "Title",
        owner: "Owner",
        date: "Date",
        switchOrder: "Switch"
      },
      typeOptions: {
        prompt: "Type",
        mapService: "Map Service",
        featureService: "Feature Service",
        imageService: "Image Service",
        vectorTileService: "Vector Tile Service",
        kml: "KML",
        wms: "WMS"
      },
      resultsPane: {
        noMatch: "No results"
      },
      paging: {
        first: "<<",
        firstTip: "First",
        previous: "<",
        previousTip: "Previous",
        next: ">",
        nextTip: "Next",
        pagePattern: "Page {page}"
      },
      resultCount: {
        countPattern: "{count} {type}",
        itemSingular: "Item",
        itemPlural: "Items"
      },
      item: {
        actions: {
          add: "Add",
          remove: "Remove",
          details: "Details",
          links: "Links"
        },
        messages: {
          adding: "Adding...",
          removing: "Removing...",
          added: "Added",
          addFailed: "Add failed",
          unsupported: "Unsupported"
        },
        typeByOwnerPattern: "{type} by {owner}",
        types: {
          "Map Service": "Map Service",
          "Feature Service": "Feature Service",
          "Image Service": "Image Service",
          "Vector Tile Service": "Vector Tile Service",
          "WMS": "WMS",
          "KML": "KML"
        }
      },
      settings: {
        addTarget: "Add",
        target: {
          caption: "Catalog",
          _delete: "Delete",
          name: "Name",
          url: "URL",
          type: "Type",
          profile: "Profile",
          filter: "Filter",
          enabled: "Enabled",
          useProxy: "Use Proxy",
          disableContentType: "Disable Content-Type Header",
          examples: "Examples"
        }
      }
    }
  })
});
