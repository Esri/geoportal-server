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
package com.esri.gpt.catalog.lucene;
import com.esri.gpt.framework.collection.StringAttribute;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Locator.
 */
public class Locator {

  /** geolocator service URL **/
  protected String url;

  /**
   * Creates new instance of the locator.
   * @return locator
   * @throws IllegalArgumentException if "lucene.locatorClass" parameter is invalid
   */
  public static Locator newInstance() {
    ApplicationContext appCtx = ApplicationContext.getInstance();
    ApplicationConfiguration appCfg = appCtx.getConfiguration();

    String locatorClassName = appCfg.getCatalogConfiguration().getParameters().getValue("lucene.locatorClass");

    if (Val.chkStr(locatorClassName).length() == 0) {
      return new Locator();
    } else {
      try {
        Class locatorClass = Class.forName(locatorClassName);
        return (Locator) locatorClass.newInstance();
      } catch (Exception ex) {
        throw new IllegalArgumentException("Invalid lucene.locatorClass parameter: " + locatorClassName);
      }
    }
  }

  /**
   * Finds candidates.
   * @param text address text
   * @return array of candidates
   * @throws IOException if accessing geolocator service fails
   * @throws ParseException if parsing address text fails
   */
  public Candidate[] find(String text) throws IOException, ParseException {
    String address = null, city = null, state = null, zip = null;

    LinkedList<String> elements = new LinkedList<String>();
    elements.addAll(Arrays.asList(Val.chkStr(text).split(",")));

    if (elements.size() >= 3) {
      zip = elements.removeLast();
      state = elements.removeLast();
      city = elements.removeLast();
    } else if (elements.size() == 2) {
      state = elements.removeLast();
      city = elements.removeLast();
    } else {
      city = elements.removeLast();
    }
    // all the rest is just address
    address = elements.toString();

    return find(address, city, state, zip);
  }

  /**
   * Finds best candidate from the array of candidates. Uses {@link Locator.ScoreComparator}.
   * @param candidates array of candidates
   * @return best candidate or <code>null</code> if no candidates can be found
   */
  public Candidate findBestCandidate(Candidate[] candidates) {
    return findBestCandidate(candidates, new ScoreComparator());
  }

  /**
   * Finds best candidate from the array of candidates.
   * @param candidates array of candidates
   * @param comparator comparator
   * @return best candidate or <code>null</code> if no candidates can be found
   */
  public Candidate findBestCandidate(Candidate[] candidates, Comparator<Candidate> comparator) {
    Candidate bestCandidate = null;
    if (candidates != null) {
      for (Locator.Candidate candidate : candidates) {
        if (comparator.compare(bestCandidate, candidate) > 0) {
          bestCandidate = candidate;
        }
      }
    }
    return bestCandidate;
  }

  /**
   * Creates instance of the locator.
   */
  protected Locator() {
    ApplicationContext appCtx = ApplicationContext.getInstance();
    ApplicationConfiguration appCfg = appCtx.getConfiguration();
    this.url = appCfg.getInteractiveMap().getLocatorUrl();
  }

  /**
   * Finds all candidates for the address.
   * @param address address
   * @param city city
   * @param state state
   * @param zip zip
   * @return array of candidates
   * @throws IOException if accessing geolocator service fails
   */
  protected Candidate[] find(String address, String city, String state, String zip) throws IOException {

    ArrayList<Candidate> candidates = new ArrayList<Candidate>();

    address = Val.chkStr(address);
    city = Val.chkStr(city);
    state = Val.chkStr(state);
    zip = Val.chkStr(zip);

    String requestUrl = this.makeQueryUrl(address, city, state, zip);

    HttpClientRequest client = HttpClientRequest.newRequest();
    client.setUrl(requestUrl);
    String sResponse = Val.chkStr(client.readResponseAsCharacters());

    if (sResponse.length() > 0) {
      try {

        JSONObject jso = new JSONObject(sResponse);
        JSONArray candidatesArr = jso.getJSONArray("candidates");
        for (int i = 0; i < candidatesArr.length(); i++) {
          JSONObject candidateObj = candidatesArr.getJSONObject(i);
          String candidateAddress = candidateObj.getString("address");
          JSONObject candidateLocation = candidateObj.getJSONObject("location");
          long candidateScore = candidateObj.getLong("score");
          JSONObject candidateAttributes = candidateObj.getJSONObject("attributes");
          double candidateX = candidateLocation.getDouble("x");
          double candidateY = candidateLocation.getDouble("y");
          StringAttributeMap attributes = new StringAttributeMap();
          JSONArray names = candidateAttributes.names();
          for (int n = 0; n < (names != null ? names.length() : 0); n++) {
            String name = names.getString(n);
            String value = candidateAttributes.getString(name);
            attributes.add(new StringAttribute(name, value));
          }

          Candidate candidate = new Candidate();
          candidate.setAddress(candidateAddress);
          candidate.setLocation(new double[]{candidateX, candidateY});
          candidate.setScore(candidateScore);
          candidate.setAttributes(attributes);

          candidates.add(candidate);
        }

      } catch (JSONException ex) {
        throw new IOException("Error reading response: "+ex.getMessage());
      }
    }

    return candidates.toArray(new Candidate[candidates.size()]);
  }

  /**
   * Makes query URL.
   * @param address address
   * @param city city
   * @param state state
   * @param zip zip
   * @return query url
   */
  protected String makeQueryUrl(String address, String city, String state, String zip) {
    StringBuilder query = new StringBuilder();
    if (address.length()>0) {
      if (query.length() > 0) {
        query.append("&");
      }
      query.append("Address=" + encode(address));
    }
    if (city.length()>0) {
      if (query.length() > 0) {
        query.append("&");
      }
      query.append("City=" + encode(city));
    }
    if (state.length()>0) {
      if (query.length() > 0) {
        query.append("&");
      }
      query.append("State=" + encode(state));
    }
    if (zip.length()>0) {
      if (query.length() > 0) {
        query.append("&");
      }
      query.append("Zip=" + encode(zip));
    }
    if (query.length() > 0) {
      query.append("&");
    }
    query.append("f=pjson");

    return url + "/findAddressCandidates?" + query;
  }

  /**
   * Encodes string.
   * @param s string to encode
   * @return encoded string
   */
  private String encode(String s) {
    try {
      return URLEncoder.encode(s, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      return s;
    }
  }

  /**
   * Score comparator.
   */
  public static class ScoreComparator implements Comparator<Candidate> {

    public int compare(Candidate o1, Candidate o2) {
      if (o1 == null && o2 == null) {
        return 0;
      }
      if (o1 == null) {
        return 1;
      }
      if (o2 == null) {
        return -1;
      }
      if (o1.getScore() == o2.getScore()) {
        return 0;
      }
      return o1.getScore() < o2.getScore() ? 1 : -1;
    }
  };

  /**
   * Candidate.
   */
  public static class Candidate {

    private String address;
    private double[] location;
    private long score;
    private StringAttributeMap attributes;

    /**
     * Gets address.
     * @return the address
     */
    public String getAddress() {
      return address;
    }

    /**
     * Sets address.
     * @param address the address to set
     */
    void setAddress(String address) {
      this.address = address;
    }

    /**
     * Gets point.
     * @return the point
     */
    public double[] getLocation() {
      return location;
    }

    /**
     * Sets point
     * @param point the point to set
     */
    void setLocation(double[] point) {
      this.location = point;
    }

    /**
     * Gets score.
     * @return the score
     */
    public long getScore() {
      return score;
    }

    /**
     * Sets score.
     * @param score the score to set
     */
    void setScore(long score) {
      this.score = score;
    }

    /**
     * Gets attributes.
     * @return the attributes
     */
    public StringAttributeMap getAttributes() {
      return attributes;
    }

    /**
     * Sets attributes.
     * @param attributes the attributes to set
     */
    void setAttributes(StringAttributeMap attributes) {
      this.attributes = attributes;
    }
  }
}
