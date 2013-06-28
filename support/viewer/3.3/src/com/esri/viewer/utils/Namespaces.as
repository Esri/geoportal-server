///////////////////////////////////////////////////////////////////////////
// Copyright (c) 2010-2011 Esri. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
///////////////////////////////////////////////////////////////////////////
package com.esri.viewer.utils
{

/**
 * Namespaces...
 *
 * @private
 */
public class Namespaces
{
    public static const RDF_NS:Namespace = new Namespace("http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    public static const DC_NS:Namespace = new Namespace("http://purl.org/dc/elements/1.1/");
    public static const SY_NS:Namespace = new Namespace("http://purl.org/rss/1.0/modules/syndication/");
    public static const CO_NS:Namespace = new Namespace("http://purl.org/rss/1.0/modules/company/");
    public static const TI_NS:Namespace = new Namespace("http://purl.org/rss/1.0/modules/textinput/");
    public static const RSS_NS:Namespace = new Namespace("http://purl.org/rss/1.0/");
    public static const ATOM_NS:Namespace = new Namespace("http://www.w3.org/2005/Atom");
    public static const ATOM_03_NS:Namespace = new Namespace("http://purl.org/atom/ns#");
    public static const XHTML_NS:Namespace = new Namespace("http://www.w3.org/1999/xhtml");
    public static const CONTENT_NS:Namespace = new Namespace("http://purl.org/rss/1.0/modules/content/");
    public static const GEORSS_NS:Namespace = new Namespace("http://www.georss.org/georss");
    public static const GEO_NS:Namespace = new Namespace("http://www.w3.org/2003/01/geo/");
    public static const GML_NS:Namespace = new Namespace("http://www.opengis.net/gml");
    public static const GEO_LL:Namespace = new Namespace("http://www.w3.org/2003/01/geo/wgs84_pos#");

    public function Namespaces(singletonEnforcer:SingletonEnforcer)
    {
    }
}

}

class SingletonEnforcer
{
}
