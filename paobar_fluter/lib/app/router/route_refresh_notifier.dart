import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

/// 把 Bloc/Cubit 的 stream 桥接成 ChangeNotifier，供 GoRouter.refreshListenable。
/// 这样 AuthCubit 一旦变更状态，GoRouter 立刻重新计算 redirect。
class BlocRefreshNotifier extends ChangeNotifier {
  BlocRefreshNotifier(BlocBase<dynamic> bloc) {
    _sub = bloc.stream.asBroadcastStream().listen((_) => notifyListeners());
  }

  late final StreamSubscription<dynamic> _sub;

  @override
  void dispose() {
    _sub.cancel();
    super.dispose();
  }
}
