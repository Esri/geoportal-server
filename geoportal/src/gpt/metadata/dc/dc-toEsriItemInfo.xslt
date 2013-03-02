<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:ows="http://www.opengis.net/ows">
  <xsl:output method="xml"/>

  <xsl:template match="/">
    <ESRI_ItemInformation>
      <!-- PARAMETERS -->
      <!-- file	
        The file to be uploaded to the geowarehouse. If uploading a file, the request must be a multi-part request. 
        Either the file, the text or the url must be specified. 
      -->
      <!-- url	
        The URL of the item to be submitted to the geowarehouse. The URL could be a URL to a service, to a web mapping application, or to any other content available at that URL. 
        Either the file, the text or the url must be specified. 
      -->
      <xsl:if test="string-length(normalize-space(/rdf:RDF/rdf:Description/dct:references))>0">
        <url>
          <xsl:value-of select="/rdf:RDF/rdf:Description/dct:references"/>
        </url>
      </xsl:if>
      <!-- text	
         The text content for the item to be submitted to the geowarehouse.
         For text content, the item parameter must be specified. This is the file name used to store the text content in the user's folder. 
         Either the file, the text or the url must be specified. 
      -->
      <xsl:if test="string-length(/rdf:RDF/rdf:Description/dct:references)=0">
        <text>
          <xsl:value-of select="/rdf:RDF/rdf:Description/dc:description"/>
        </text>
      </xsl:if>
      <!-- item	
         The file name used to store the text content in the user's folder. 
         For text content, the item parameter must be specified. This is the file name used to store the text content in the user's folder. 
         If the name contains invalid filesystem characters, those characters will be replaced with an underscore. 
      -->
      <xsl:if test="string-length(/rdf:RDF/rdf:Description/dct:references)=0">
        <item>
          <xsl:value-of select="translate(normalize-space(/rdf:RDF/rdf:Description/dc:title),' ','_')"/>
        </item>
      </xsl:if>
      <!-- relationshipType	
         The type of relationship between the two items. See Relationship Types for a complete listing of types.
      -->
      <!-- originItemId	
         The item id of the 'origin' item of the relationship.
      -->
      <!-- destinationItemId	
         The item id of the 'destination' item of the relationship.
      -->
      <!-- overwrite	
         If true, an existing file in this folder with the same name as the one being uploaded will be overwritten. If false and if a file with the same name exists in this folder, it is not overwritten and instead an error is generated. 
      -->
      <!-- async	
         If true, the file is uploaded asynchronously. If false, the file is uploaded synchronously.
      -->
      
      <!-- ITEM FIELDS -->
      <!-- name	
         Name of the item. This is a deprecated field.
      -->
      <!-- access	
         Sets the access level on the item. private is the default and only the item owner can access. shared allows the item to be shared with a specific group. Setting to account restricts item access to members of your organization. If public, all users can access the item.
         Values: private | shared | account | public
      -->
      <access>public</access>
      <!-- title	
         Title of the item.
      -->
      <title>
        <xsl:value-of select="/rdf:RDF/rdf:Description/dc:title"/>
      </title>
      <!-- thumbnail	
         Enter the pathname to the thumbnail image to be used for the item. The recommended image size is 200 pixels wide by 133 pixels high. Acceptable image formats are: PNG, GIF, and JPEG. The maximum file size for an image is 1 MB. This is not a reference to the file but the file itself which will be stored on the Sharing servers.
      -->
      <!-- thumbnailurl	
         Enter the URL to the thumbnail image to be used for the item. The recommended image size is 200 pixels wide by 133 pixels high. Acceptable image formats are: PNG, GIF, and JPEG. The maximum file size for an image is 1 MB.
      -->
      <!-- metadata	
         The file that stores the metadata information on an item. It is stored in the metadata folder under esriinfo, e.g., /sharing/content/items/<itemid>/info/metadata/metadata.xml.
      -->
      <!-- type	
         The type of item and is a pre-defined field. See Item Types for a listing of the different types.
      -->
      <!-- typekeywords	
         Type keywords describe the type. See Item Types for a listing of the different pre-defined types or create your own.
         Syntax: typekeywords=<keyword1>, <keyword2>
      -->
      <!-- description	
         An item description can be of any length.
      -->
      <description>
        <xsl:value-of select="/rdf:RDF/rdf:Description/dc:description"/>
      </description>
      <!-- tags	
         Tags are words or short phrases that describe your item. Separate terms with commas.
         Syntax: tags=<tag1>, <tag2>
      -->
      <!-- snippet	
         Snippet or summary of the item and is limited to 250 characters.
      -->
      <!-- extent	
         The bounding rectangle of the item.
         Syntax: extent=<xmin>, <ymin>, <xmax>, <ymax>
      -->
      <extent>
        <xsl:value-of select="translate(normalize-space(/rdf:RDF/rdf:Description/ows:WGS84BoundingBox/ows:LowerCorner),' ',',')"/>,
        <xsl:value-of select="translate(normalize-space(/rdf:RDF/rdf:Description/ows:WGS84BoundingBox/ows:UpperCorner),' ',',')"/>
      </extent>
      <!-- spatialreference	
         The coordinate system of the item.
      -->
      <spatialreference>4326</spatialreference>
      <!-- accessinformation	
         Credit the source of your item.
      -->
      <!-- licenseinfo	
         Include any license information or restrictions.
      -->
      <!-- culture	
         The item locale (language and country) information.
      -->
      <culture><xsl:value-of select="/rdf:RDF/rdf:Description/dc:language"/></culture>
    </ESRI_ItemInformation>
  </xsl:template>

</xsl:stylesheet>
