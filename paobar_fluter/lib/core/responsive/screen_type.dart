import 'package:flutter/widgets.dart';
import 'package:paobar/core/responsive/breakpoints.dart';

enum ScreenType { compact, medium, expanded, large }

extension ScreenTypeX on ScreenType {
  bool get isCompact => this == ScreenType.compact;
  bool get isAtLeastMedium => index >= ScreenType.medium.index;
  bool get isAtLeastExpanded => index >= ScreenType.expanded.index;
  bool get isAtLeastLarge => index >= ScreenType.large.index;
}

ScreenType screenTypeOf(double width) {
  if (width < Breakpoints.compact) return ScreenType.compact;
  if (width < Breakpoints.medium) return ScreenType.medium;
  if (width < Breakpoints.expanded) return ScreenType.expanded;
  return ScreenType.large;
}

extension ScreenTypeContext on BuildContext {
  ScreenType get screenType => screenTypeOf(MediaQuery.sizeOf(this).width);
}
