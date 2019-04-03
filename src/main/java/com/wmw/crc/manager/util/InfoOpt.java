/*
 *
 * Copyright 2018 Wei-Ming Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.wmw.crc.manager.util;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import com.google.common.base.Strings;

public final class InfoOpt<T> {

  private static final InfoOpt<?> EMPTY = new InfoOpt<>();

  private final T value;

  private final String infomation;

  public String getInfomation() {
    return infomation;
  }

  public boolean hasInformation() {
    return !infomation.isEmpty();
  }

  private InfoOpt() {
    this.value = null;
    this.infomation = null;
  }

  public static <T> InfoOpt<T> empty() {
    @SuppressWarnings("unchecked")
    InfoOpt<T> t = (InfoOpt<T>) EMPTY;
    return t;
  }

  private InfoOpt(T value, String infomation) {
    this.value = value;
    this.infomation = Strings.nullToEmpty(infomation);
  }

  public static <T> InfoOpt<T> of(T value) {
    return new InfoOpt<>(Objects.requireNonNull(value), null);
  }

  public static <T> InfoOpt<T> of(T value, String infomation) {
    return new InfoOpt<>(Objects.requireNonNull(value), infomation);
  }

  public static <T> InfoOpt<T> ofNullable(T value) {
    return new InfoOpt<>(value, null);
  }

  public static <T> InfoOpt<T> ofNullable(T value, String infomation) {
    return new InfoOpt<>(value, infomation);
  }

  public T get() {
    if (value == null) {
      throw new NoSuchElementException("No value present");
    }
    return value;
  }

  public boolean isPresent() {
    return value != null;
  }

  public boolean isAbsent() {
    return value == null;
  }

  public void ifPresent(Consumer<? super T> action) {
    if (value != null) {
      action.accept(value);
    }
  }

  public void ifPresentOrElse(Consumer<? super T> action,
      Runnable emptyAction) {
    if (value != null) {
      action.accept(value);
    } else {
      emptyAction.run();
    }
  }

  public InfoOpt<T> filter(Predicate<? super T> predicate) {
    Objects.requireNonNull(predicate);
    if (!isPresent()) {
      return this;
    } else {
      return predicate.test(value) ? this : empty();
    }
  }

  public <U> InfoOpt<U> map(Function<? super T, ? extends U> mapper) {
    Objects.requireNonNull(mapper);
    if (!isPresent()) {
      return empty();
    } else {
      return InfoOpt.ofNullable(mapper.apply(value));
    }
  }

  public <U> InfoOpt<U> flatMap(
      Function<? super T, ? extends InfoOpt<? extends U>> mapper) {
    Objects.requireNonNull(mapper);
    if (!isPresent()) {
      return empty();
    } else {
      @SuppressWarnings("unchecked")
      InfoOpt<U> r = (InfoOpt<U>) mapper.apply(value);
      return Objects.requireNonNull(r);
    }
  }

  public InfoOpt<T> or(Supplier<? extends InfoOpt<? extends T>> supplier) {
    Objects.requireNonNull(supplier);
    if (isPresent()) {
      return this;
    } else {
      @SuppressWarnings("unchecked")
      InfoOpt<T> r = (InfoOpt<T>) supplier.get();
      return Objects.requireNonNull(r);
    }
  }

  public Stream<T> stream() {
    if (!isPresent()) {
      return Stream.empty();
    } else {
      return Stream.of(value);
    }
  }

  public T orElse(T other) {
    return value != null ? value : other;
  }

  public T orElseGet(Supplier<? extends T> supplier) {
    return value != null ? value : supplier.get();
  }

  public <X extends Throwable> T orElseThrow(
      Supplier<? extends X> exceptionSupplier) throws X {
    if (value != null) {
      return value;
    } else {
      throw exceptionSupplier.get();
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof InfoOpt)) {
      return false;
    }

    InfoOpt<?> other = (InfoOpt<?>) obj;
    return Objects.equals(value, other.value);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }

  @Override
  public String toString() {
    return value != null ? String.format("InfoOpt[%s]", value)
        : "InfoOpt.empty";
  }

}
