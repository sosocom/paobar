import 'package:flutter/widgets.dart';

/// 内容自适应缩放 + 捏合缩放。
/// 逻辑对齐 Web 的 `calcContentScale`：
///   - 内容原始高度 < 可用高度 → 放大铺满（受 MAX_FIT 限制）
///   - 用户捏合手势可在 [minScale, maxScale] 内覆盖
class TabScaleController extends ChangeNotifier {
  TabScaleController({
    this.minScale = 0.8,
    this.maxScale = 2.5,
    this.maxFit = 1.35,
  });

  /// 手势可用的最小缩放
  final double minScale;

  /// 手势可用的最大缩放
  final double maxScale;

  /// 自动铺满时的最大倍率，防止短谱被拉到视觉畸形
  final double maxFit;

  double _scale = 1;
  double get scale => _scale;

  /// 短谱自动铺满策略。
  void autoFit({required double contentHeight, required double availableHeight}) {
    if (contentHeight <= 0 || availableHeight <= 0) {
      _setScale(1);
      return;
    }
    if (contentHeight >= availableHeight) {
      _setScale(1);
      return;
    }
    final fit = availableHeight / contentHeight;
    _setScale(fit.clamp(1.0, maxFit));
  }

  /// 手势传入的新 scale。
  void setPinch(double newScale) {
    _setScale(newScale.clamp(minScale, maxScale));
  }

  void reset() => _setScale(1);

  void _setScale(double v) {
    if ((v - _scale).abs() < 0.001) return;
    _scale = v;
    notifyListeners();
  }
}
