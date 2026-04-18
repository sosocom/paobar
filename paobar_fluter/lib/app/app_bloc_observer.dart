import 'package:bloc/bloc.dart';
import 'package:logger/logger.dart';
import 'package:paobar/core/config/env.dart';
import 'package:paobar/core/di/injector.dart';

class AppBlocObserver extends BlocObserver {
  final Logger _log = sl<Logger>();

  @override
  void onChange(BlocBase<dynamic> bloc, Change<dynamic> change) {
    super.onChange(bloc, change);
    if (!Env.I.enableLogging) return;
    _log.d('[${bloc.runtimeType}] ${change.currentState} → ${change.nextState}');
  }

  @override
  void onError(BlocBase<dynamic> bloc, Object error, StackTrace stackTrace) {
    _log.e('[${bloc.runtimeType}] error', error: error, stackTrace: stackTrace);
    super.onError(bloc, error, stackTrace);
  }
}
