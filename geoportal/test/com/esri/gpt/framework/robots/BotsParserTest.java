/*
 * Copyright 2015 pete5162.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.framework.robots;

import static com.esri.gpt.framework.robots.BotsUtils.requestAccess;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author pete5162
 */
public class BotsParserTest {
  private String robotsTxt;
  
  @Before
  public void setUp() throws IOException {
    InputStream inputRobots = Thread.currentThread().getContextClassLoader().getResourceAsStream("com/esri/gpt/framework/robots/robots.txt");
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    IOUtils.copy(inputRobots, buffer);
    IOUtils.closeQuietly(inputRobots);
    IOUtils.closeQuietly(buffer);
    
    robotsTxt = buffer.toString("UTF-8");
  }
  
  @Test
  public void testGeoportalServerUserAgent() throws IOException {
    BotsParser parser = new BotsParser(true, true, "GeoportalServer");
    
    InputStream input = new ByteArrayInputStream(robotsTxt.getBytes("UTF-8"));
    Bots bots = parser.readRobotsTxt(BotsMode.always, WinningStrategy.LONGEST_PATH, input);
    assertNotNull(bots);
    
    assertTrue(!requestAccess(bots,"http://www.fict.org/").hasAccess());
    assertTrue(!requestAccess(bots,"http://www.fict.org/index.html").hasAccess());
    assertTrue(requestAccess(bots,"http://www.fict.org/robots.txt").hasAccess());
    assertTrue(requestAccess(bots,"http://www.fict.org/server.html").hasAccess());
    assertTrue(requestAccess(bots,"http://www.fict.org/services/fast.html").hasAccess());
    assertTrue(requestAccess(bots,"http://www.fict.org/services/slow.html").hasAccess());
    assertTrue(!requestAccess(bots,"http://www.fict.org/orgo.gif").hasAccess());
    assertTrue(requestAccess(bots,"http://www.fict.org/org/about.html").hasAccess());
    assertTrue(!requestAccess(bots,"http://www.fict.org/org/plans.html").hasAccess());
    assertTrue(!requestAccess(bots,"http://www.fict.org/%7Ejim/jim.html").hasAccess());
    assertTrue(requestAccess(bots,"http://www.fict.org/%7Emak/mak.html").hasAccess());
  }
  
}
