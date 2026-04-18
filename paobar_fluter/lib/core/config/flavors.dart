enum Flavor {
  dev,
  staging,
  prod;

  static Flavor fromString(String? value) {
    switch (value?.toLowerCase()) {
      case 'prod':
      case 'production':
        return Flavor.prod;
      case 'staging':
      case 'stg':
        return Flavor.staging;
      case 'dev':
      case 'development':
      default:
        return Flavor.dev;
    }
  }

  bool get isDev => this == Flavor.dev;
  bool get isProd => this == Flavor.prod;
}
