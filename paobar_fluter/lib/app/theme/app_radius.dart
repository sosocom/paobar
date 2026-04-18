import 'package:flutter/widgets.dart';

abstract final class AppRadius {
  AppRadius._();

  static const double xs = 4;
  static const double sm = 6;
  static const double md = 8;
  static const double lg = 12;
  static const double xl = 16;
  static const double xxl = 24;
  static const double pill = 9999;

  static const BorderRadius br4 = BorderRadius.all(Radius.circular(xs));
  static const BorderRadius br8 = BorderRadius.all(Radius.circular(md));
  static const BorderRadius br12 = BorderRadius.all(Radius.circular(lg));
  static const BorderRadius br16 = BorderRadius.all(Radius.circular(xl));
  static const BorderRadius brPill = BorderRadius.all(Radius.circular(pill));
}
