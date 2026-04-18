import 'package:freezed_annotation/freezed_annotation.dart';
import 'package:paobar/core/errors/failure.dart';

part 'result.freezed.dart';

/// `Result<T>` = Success<T> | Error<Failure>。
/// UseCase/Repository 更倾向用 Result 而不是 throw。
@freezed
sealed class Result<T> with _$Result<T> {
  const Result._();

  const factory Result.success(T value) = Success<T>;
  const factory Result.error(Failure failure) = Error<T>;

  bool get isSuccess => this is Success<T>;
  bool get isError => this is Error<T>;

  T? get valueOrNull => this is Success<T> ? (this as Success<T>).value : null;
  Failure? get failureOrNull =>
      this is Error<T> ? (this as Error<T>).failure : null;

  R fold<R>(R Function(T value) onSuccess, R Function(Failure failure) onError) {
    final self = this;
    if (self is Success<T>) return onSuccess(self.value);
    return onError((self as Error<T>).failure);
  }
}
