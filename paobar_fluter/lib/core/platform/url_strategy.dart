// 跨端 URL 策略入口：仅 Web 生效，其它端为 no-op。

export 'url_strategy_stub.dart'
    if (dart.library.html) 'url_strategy_web.dart';
