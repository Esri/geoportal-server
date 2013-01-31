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
package com.esri.gpt.framework.util;

import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.TimePeriod.DayOfWeek;
import com.esri.gpt.framework.util.TimePeriod.Unit;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Time period.
 * Represents time period in one of various forms indicating a number of
 * milliseconds to elapse until defined moment in time.</br>
 * Generic way of creating instance of the class is to use static method
 * {@link TimePeriod#parseValue(java.lang.String)}. That method understands several
 * different ways of expressing a time period.
 * </p>
 * The simplest way of creating time period is by providing number of milliseconds.
 * Use a simple integer number like this: </br>
 * <pre>
 *  TimePeriod.parse("1000");</pre>
 * to create a a constant, one second period.
 * </p>
 * Another way to create period is to use one of the values from {@link TimePeriod.Unit}.
 * Combining integer value with the unit name simplifies expressing longer periods, for example:</br>
 * <pre>
 *  TimePeriod.parse("2[DAY]");</pre>
 * gives just two days constant period.
 * </p>
 * There is also a way to express relative periods. Giving:
 * <pre>
 *  TimePeriod.parse("08:00");</pre>
 * gives a period from now on to the closest eight o'clock am every day.
 * </p>
 * It is also possible to use one of the values of {@link TimePeriod.DayOfWeek}, like this:
 * <pre>
 *  TimePeriod.parse("monday");</pre>
 * which means period from now on to the begining of the closes Monday, or even more specific way:
 * <pre>
 *  TimePeriod.parse("monday 08:00");</pre>
 * to have period from now on to the eight o'clock am of the Monday.
 * </p>
 * It is also possible to parse array of time periods:</br>
 * <pre>
 *  TimePeriod.parse("08:00,sunday 10:00");</pre>
 * which gives period of eight o'clock every day and ten o'clock every sunday, whatever
 * comes first.
 */
public final class TimePeriod implements Serializable {

// class variables =============================================================
// instance variables ==========================================================
/** wait time provider */
private WaitTimeProvider waitTimeProvider;

// constructors ================================================================
/**
 * Creates instance of time period.
 */
public TimePeriod() {
}

/**
 * Creates instance of time period.
 * @param value value in milliseconds
 */
public TimePeriod(long value) {
  setValue(value);
}

/**
 * Creates instance of time period.
 * @param value value in milliseconds
 * @param unit unit
 */
public TimePeriod(long value, Unit unit) {
  setValue(value, unit);
}

/**
 * Creates instance of time period.
 * @param waitTimeProvider wait time provider
 */
private TimePeriod(WaitTimeProvider waitTimeProvider) {
  this.waitTimeProvider = waitTimeProvider;
}
// properties ==================================================================

/**
 * Gets value.
 * @return value in milliseconds
 */
public long getValue() {
  return waitTimeProvider != null ? waitTimeProvider.getWaitTime(new Date()) : 0;
}

/**
 * Gets value.
 * @param since since
 * @return value in milliseconds
 */
public long getValue(Date since) {
  return waitTimeProvider != null ? waitTimeProvider.getWaitTime(since) : 0;
}

/**
 * Sets value.
 * @param value value in milliseconds
 */
public void setValue(long value) {
  waitTimeProvider = new LongWaitTimeProvider(value);
}

/**
 * Sets value.
 * @param value value
 * @param unit unit
 */
public void setValue(long value, Unit unit) {
  waitTimeProvider = new UnitWaitTimeProvider(value, unit);
}
// methods =====================================================================

/**
 * Parses value into time period.
 * @param value value to parse
 * @return time period
 * @throws IllegalArgumentException if unable to parse value
 */
public static TimePeriod parseValue(String value)
    throws IllegalArgumentException {
  
  WaitTimeProvider wtp = SnowBallWaitTimeProvider.parse(value);
  if (wtp!=null)
    return new TimePeriod(wtp);

  wtp = WaitTimeProviderFactory.create(value);
  if (wtp!=null)
    return new TimePeriod(wtp);

  throw new IllegalArgumentException("Illegal period value: " + value);
}

/**
 * Creates string representation of the object.
 * @return string representation of the object
 */
@Override
public String toString() {
  long value = getValue();
  StringBuilder sb = new StringBuilder();

  Unit[] vals = Unit.values();
  for (int i = vals.length - 1; i >= 0; i--) {
    Unit u = vals[i];
    if (!u.isToStringeable())
      continue;
    if (value >= u.getMilliseconds()) {
      long part = value / u.getMilliseconds();
      value = value % u.getMilliseconds();
      if (sb.length() > 0)
        sb.append(", ");
        sb.append(Long.toString(part)).append(" ").append(u.name().toLowerCase()).append(part > 1 ? "s" : "");
    }
  }

  return sb.length() > 0 ? sb.toString() : "0";
}

/**
 * Converts into localized string.
 * @param mb message broker
 * @return localized string
 */
public String toLocalizedString(MessageBroker mb) {
  long value = getValue();
  StringBuilder sb = new StringBuilder();

  Unit[] vals = Unit.values();

  for (int i = vals.length - 1; i >= 0; i--) {
    Unit u = vals[i];
    if (!u.isToLocalizedStringeable())
      continue;
    if (value >= u.getMilliseconds()) {
      long part = value / u.getMilliseconds();
      value = value % u.getMilliseconds();
      if (part>0) {
        String resKey = u.getResourceKey();
        if (part>1) {
          resKey += "s"; // for plural
        }
        String message = mb.retrieveMessage(resKey, new String[]{Long.toString(part)});
        if (message.length()>0) {
          if (sb.length() > 0)
            sb.append(", ");
          sb.append(message);
        }
      }
    }
  }

  return sb.toString();
}

// enums =======================================================================
/**
 * Time unit.
 */
public enum Unit {

  /** millisecond */
  millisecond(1L, true, false),
  /** second */
  second(1000L),
  /** minute */
  minute(60000L),
  /** hour */
  hour(3600000L),
  /** day */
  day(86400000L),
  /** week */
  week(604800000L, false, false),
  /** month */
  month(2592000000L);

  /** number of milliseconds in one unit */
  private long _milliseconds;
  /** ability to use in toString */
  private boolean _toStringeable = true;
  /** ability to use in toLocalizdString */
  private boolean _toLocalizedStringeable = true;

  /**
   * Creates instance of the unit.
   * @param milliseconds number of milliseconds in one unit
   */
  Unit(long milliseconds) {
    _milliseconds = milliseconds;
  }

  /**
   * Creates instance of the unit.
   * @param milliseconds number of milliseconds in one unit
   * @param toStringeable <code>true</code> if can be used in toString
   */
  Unit(long milliseconds, boolean toStringeable, boolean toLocalizedStringeable) {
    _milliseconds = milliseconds;
    _toStringeable = toStringeable;
    _toLocalizedStringeable = toLocalizedStringeable;
  }

  /**
   * Checks string value.
   * @param value string representation of the value
   * @return value represented by string or {@link Unit#millisecond} if value
   * doesn't represent valid unit
   */
  public static Unit checkValueOf(String value) {
    value = Val.chkStr(value);
    for (Unit u : values()) {
      if (u.name().equalsIgnoreCase(value)) {
        return u;
      }
    }
    return millisecond;
  }

  /**
   * Gets milliseconds.
   * @return number of milliseconds in one unit
   */
  public long getMilliseconds() {
    return _milliseconds;
  }

  public boolean isToStringeable() {
    return _toStringeable;
  }

  public boolean isToLocalizedStringeable() {
    return _toLocalizedStringeable;
  }

  private String getResourceKey() {
    return "catalog.TimePeriod.Unit" + "." + name();
  }
}

/**
 * Day of the week.
 */
public enum DayOfWeek {

  Sunday(new String[]{"Sun", "Su"}),
  Monday(new String[]{"Mon", "Mo"}),
  Tuesday(new String[]{"Tue", "Tu"}),
  Wednesday(new String[]{"Wed", "We"}),
  Thursday(new String[]{"Thu", "Th"}),
  Friday(new String[]{"Fri", "Fr"}),
  Saturday(new String[]{"Sat", "Sa"}),;
  private String[] altNames;

  DayOfWeek(String[] altNames) {
    this.altNames = altNames;
  }

  public static DayOfWeek checkValueOf(String value) {
    value = Val.chkStr(value);
    for (DayOfWeek u : values()) {
      if (u.is(value)) {
        return u;
      }
    }
    return null;
  }

  private boolean is(String name) {
    if (name().equalsIgnoreCase(name)) {
      return true;
    }
    for (String altName : altNames) {
      if (altName.equalsIgnoreCase(name)) {
        return true;
      }
    }
    return false;
  }
}
}

/**
 * Wait time provider factory.
 */
class WaitTimeProviderFactory {

  /**
   * Creates wait time provider based on the value.
   * @param value value
   * @return instance of the wait time provider or <code>null</code> if value can not be parsed
   */
  public static WaitTimeProvider create(String value) {
    WaitTimeProvider wtp;

    wtp = LongWaitTimeProvider.parse(value);
    if (wtp != null) return wtp;

    wtp = UnitWaitTimeProvider.parse(value);
    if (wtp != null) return wtp;

    wtp = DayOfWeekTimeProvider.parse(value);
    if (wtp != null) return wtp;

    wtp = HourOfDayTimeProvider.parse(value);
    if (wtp != null) return wtp;

    return null;
  }
}

/**
 * Wait time provider.
 */
interface WaitTimeProvider extends Serializable {

/**
 * Gets wait time.
 * @param since since date
 * @return wait time in milliseconds
 */
long getWaitTime(Date since);
}

/**
 * Wait time provider by long value.
 */
class LongWaitTimeProvider implements WaitTimeProvider {

/** wait time */
private long waitTime;

/**
 * Creates instance of the provider.
 * @param waitTime wait time given as value in milliseconds
 */
public LongWaitTimeProvider(long waitTime) {
  this.waitTime = waitTime;
}

@Override
public long getWaitTime(Date since) {
  return waitTime;
}

/**
 * Parses value into wait time provider.
 * @param value value
 * @return provider or <code>null</code> if can not be parsed
 */
public static WaitTimeProvider parse(String value) {
  value = Val.chkStr(value);
  try {
    return new LongWaitTimeProvider(Long.parseLong(value));
  } catch (NumberFormatException ex) {
    return null;
  }
}
}

/**
 * Wait time provider for time unit.
 */
class UnitWaitTimeProvider implements WaitTimeProvider {

/** unit */
private Unit unit;
/** value */
private long value;

/**
 * Creates instance of the provider.
 * @param value value
 * @param unit unit
 */
public UnitWaitTimeProvider(long value, Unit unit) {
  this.unit = unit;
  this.value = value;
}

@Override
public long getWaitTime(Date since) {
  return value * unit.getMilliseconds();
}

/**
 * Parses value into wait time provider.
 * @param value value
 * @return provider or <code>null</code> if can not be parsed
 */
public static WaitTimeProvider parse(String value) {
  value = Val.chkStr(value).toLowerCase();
  Unit unit = null;

  for (Unit u : Unit.values()) {
    String patternDef =
        "\\[\\p{Blank}*" + u.name().toLowerCase() + "\\p{Blank}*\\]";
    Pattern pattern = Pattern.compile(patternDef);
    Matcher matcher = pattern.matcher(value);
    if (matcher.find()) {
      int index = matcher.start();
      if (index >= 0) {
        unit = u;
        value = value.substring(0, index).trim();
        break;
      }
    }
  }

  if (unit != null) {
    try {
      return new UnitWaitTimeProvider( Long.parseLong(value), unit);
    } catch (NumberFormatException ex) {
      return null;
    }
  } else {
    return null;
  }
}
}

/**
 * Hour of the day time provider.
 */
class HourOfDayTimeProvider implements WaitTimeProvider {
/** time of the day */
private Calendar at;

/**
 * Creates instance of the provider.
 * @param time time
 */
public HourOfDayTimeProvider(Date time) {
  this.at = createAtDate(time);
}

@Override
public long getWaitTime(Date since) {
  Calendar cal = Calendar.getInstance();
  cal.setTime(since);
  cal.set(0, 0, 0, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), 0);

  if (cal.before(at)) {
    return at.getTimeInMillis() - cal.getTimeInMillis();
  } else {
    return Unit.day.getMilliseconds() - (at.getTimeInMillis() - cal.getTimeInMillis());
  }
}

/**
 * Parses value into wait time provider.
 * @param value value
 * @return provider or <code>null</code> if can not be parsed
 */
public static WaitTimeProvider parse(String value) {
  value = Val.chkStr(value);
  String[] hourMin = value.split(":");
  if (hourMin.length == 2) {
    try {
      int h = Integer.parseInt(hourMin[0]);
      int m = Integer.parseInt(hourMin[1]);

      Calendar atCal = Calendar.getInstance();
      atCal.set(0, 0, 0, h, m, 0);

      return new HourOfDayTimeProvider(atCal.getTime());
    } catch (NumberFormatException ex) {
    }
  }
  return null;
}

/**
 * Creates time based on a date.
 * @param time base date
 * @return time
 */
private Calendar createAtDate(Date time) {
  Calendar timeCal = Calendar.getInstance();
  timeCal.setTime(time);

  Calendar cal = Calendar.getInstance();
  cal.set(0, 0, 0, timeCal.get(Calendar.HOUR_OF_DAY), timeCal.get(Calendar.MINUTE), 0);
  return cal;
}
}

/**
 * Wait time provider for a day of the week.
 */
class DayOfWeekTimeProvider implements WaitTimeProvider {

/** day of the week */
private DayOfWeek dayOfWeek;
/** time of the day */
private Calendar at;

/**
 * Creates instance of the provider.
 * @param dayOfWeek day of the week
 */
public DayOfWeekTimeProvider(DayOfWeek dayOfWeek) {
  this.dayOfWeek = dayOfWeek;
  this.at = createAtDate();
}

/**
 * Creates instance of the provider.
 * @param dayOfWeek day of the week
 * @param time time of the day
 */
public DayOfWeekTimeProvider(DayOfWeek dayOfWeek, Date time) {
  this.dayOfWeek = dayOfWeek;
  this.at = createAtDate(time);
}

@Override
public long getWaitTime(Date since) {
  Calendar cal = Calendar.getInstance();
  cal.setTime(since);
  Calendar act = Calendar.getInstance();

  int currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
  int diff = dayOfWeek.ordinal() - currentDayOfWeek;
  int span = diff >= 0 ? diff : 7 + diff;

  act.setTimeInMillis(cal.getTimeInMillis());
  act.set(Calendar.HOUR_OF_DAY, at.get(Calendar.HOUR_OF_DAY));
  act.set(Calendar.MINUTE, at.get(Calendar.MINUTE));
  act.set(Calendar.SECOND, 0);
  act.set(Calendar.MILLISECOND, 0);

  if (!(span == 6 && act.after(cal))) {
    act.add(Calendar.DAY_OF_YEAR, 1);
    long msecToDayEnd = act.getTimeInMillis() - cal.getTimeInMillis();
    long waitTime = span * Unit.day.getMilliseconds() + msecToDayEnd;
    return waitTime;
  } else {
    long waitTime = act.getTimeInMillis() - cal.getTimeInMillis();
    return waitTime;
  }
}

/**
 * Parses value into wait time provider.
 * @param value value
 * @return provider or <code>null</code> if can not be parsed
 */
public static WaitTimeProvider parse(String value) {
  value = Val.chkStr(value);
  String[] elements = value.split("[ ]+");
  if (elements.length == 1) {
    DayOfWeek dow = DayOfWeek.checkValueOf(elements[0]);
    if (dow != null) {
      return new DayOfWeekTimeProvider(dow);
    }
  } else if (elements.length == 2) {
    DayOfWeek dow = DayOfWeek.checkValueOf(elements[0]);
    String[] hourMin = elements[1].split(":");
    if (dow != null && hourMin.length == 2) {
      try {
        int h = Integer.parseInt(hourMin[0]);
        int m = Integer.parseInt(hourMin[1]);

        Calendar atCal = Calendar.getInstance();
        atCal.set(0, 0, 0, h, m, 0);

        return new DayOfWeekTimeProvider(dow, atCal.getTime());
      } catch (NumberFormatException ex) {
      }
    }
  }
  return null;
}

/**
 * Creates empty time.
 * @return empty time
 */
private Calendar createAtDate() {
  Calendar cal = Calendar.getInstance();
  cal.set(0, 0, 0, 0, 0, 0);
  return cal;
}

/**
 * Creates time based on a date.
 * @param time base date
 * @return time
 */
private Calendar createAtDate(Date time) {
  Calendar timeCal = Calendar.getInstance();
  timeCal.setTime(time);

  Calendar cal = Calendar.getInstance();
  cal.set(0, 0, 0, timeCal.get(Calendar.HOUR_OF_DAY), timeCal.get(Calendar.MINUTE), 0);
  return cal;
}
}

/**
 * 'Snow ball' wait time provider.
 */
class SnowBallWaitTimeProvider implements WaitTimeProvider {
  /** array of providers */
  private WaitTimeProvider [] providers;

  /**
   * Creates instance of the provider.
   * @param providers array of providers
   */
  public SnowBallWaitTimeProvider(WaitTimeProvider [] providers) {
    this.providers = providers!=null? providers: new WaitTimeProvider []{};
  }

  @Override
  public long getWaitTime(Date since) {
    long shortestWaitTime = -1;

    for (WaitTimeProvider p : providers) {
      long waitTime = p.getWaitTime(since);
      if (shortestWaitTime<0 || waitTime<shortestWaitTime) {
        shortestWaitTime = waitTime;
      }
    }

    return shortestWaitTime>=0? shortestWaitTime: 0;
  }

  /**
   * Parses array of time definitions into snow ball provider.
   * @param value array of time periods separated by comma (,)
   * @return snow ball provider or <code>null</code> value can not be parsed into snow ball provider
   */
  public static WaitTimeProvider parse(String value) {
    value = Val.chkStr(value);
    String [] parts = value.split(",");
    ArrayList<WaitTimeProvider> providers = new ArrayList<WaitTimeProvider>();
    for (String part : parts) {
      WaitTimeProvider wtp = WaitTimeProviderFactory.create(part);
      if (wtp!=null)
        providers.add(wtp);
    }

    return providers.size()>1? new SnowBallWaitTimeProvider(providers.toArray(new WaitTimeProvider[providers.size()])): null;
  }
}