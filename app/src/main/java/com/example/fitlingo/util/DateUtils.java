package com.example.fitlingo.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
  private static final SimpleDateFormat UI = new SimpleDateFormat("dd/MM/yyyy");
  private static final SimpleDateFormat ISO = new SimpleDateFormat("yyyy-MM-dd");

  public static String toUi(Date d) { return UI.format(d); }
  public static Date parseUi(String s) throws ParseException { return UI.parse(s); }
  public static String toIso(Date d) { return ISO.format(d); }
}
