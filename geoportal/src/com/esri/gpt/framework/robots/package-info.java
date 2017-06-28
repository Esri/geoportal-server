/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Provides support for reading and parsing robots.txt
 * <p>
 * Standard for Robots Exclusion is a mechanism allowing servers to communicate
 * with web crawlers about it's access policy. This implementation follows
 * recommendations found in the following sources:
 * <p>
 * {@linkplain <a href="http://www.robotstxt.org/orig.html">http://www.robotstxt.org/orig.html</a>}<br>
 * {@linkplain <a href="http://www.robotstxt.org/norobots-rfc.txt">http://www.robotstxt.org/norobots-rfc.txt</a>}<br>
 * {@linkplain <a href="https://en.wikipedia.org/wiki/Robots_exclusion_standard">https://en.wikipedia.org/wiki/Robots_exclusion_standard</a>}<br>
 * {@linkplain <a href="https://developers.google.com/webmasters/control-crawl-index/docs/robots_txt">https://developers.google.com/webmasters/control-crawl-index/docs/robots_txt</a>}<br>
 * <p>
 * Behavior of the "robots.txt" mechanism can be configured through the following 
 * parameters in the gpt.xml configuration file:
 * <p>
 * <b>bot.robotstxt.enabled</b>: use of robots.txt during harvesting is enabled. Default: true<br>
 * <b>bot.robotstxt.override</b>:  allows user to override bot.robotstxt.enabled. Default: true;<br>
 * <b>bot.agent</b>: name of the user agent for interpreting content of robots.txt. Default: "GeoportalServer"<br>
 * 
 * 
 * @see com.esri.gpt.framework.robots.Bots
 * @see com.esri.gpt.framework.robots.BotsParser
 * @see com.esri.gpt.framework.robots.BotsUtils
 */
package com.esri.gpt.framework.robots;
