import 'package:flutter_web_plugins/url_strategy.dart' as web;

/// Web 使用 path-based URL，不带 `#`，利于深链分享与 SEO。
void usePathUrlStrategy() => web.usePathUrlStrategy();
