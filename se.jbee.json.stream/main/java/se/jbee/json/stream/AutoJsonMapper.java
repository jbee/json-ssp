package se.jbee.json.stream;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import se.jbee.json.stream.JsonMapping.JsonTo;

final class AutoJsonMapper {

  private AutoJsonMapper() {
    throw new UnsupportedOperationException("util");
  }

  private static final Map<Class<?>, JsonTo<?>> MAPPER_BY_TO_TYPE = new ConcurrentHashMap<>();

  public static <T> void register(JsonTo<T> by) {
    MAPPER_BY_TO_TYPE.put(by.to(), by);
  }

  static {
    // TODO make this a default method "with" which normally wraps this on but on  a map it just
    // adds
    register(new JsonTo<>(String.class, Function.identity(), Objects::toString, Objects::toString));
    register(new JsonTo<>(Integer.class, Integer::valueOf, Number::intValue, b -> b ? 1 : 0));
    register(new JsonTo<>(Long.class, Long::valueOf, Number::longValue, b -> b ? 1L : 0L));
    register(new JsonTo<>(Float.class, Float::valueOf, Number::floatValue, b -> b ? 1f : 0f));
    register(new JsonTo<>(Double.class, Double::valueOf, Number::doubleValue, b -> b ? 1d : 0d));
    register(new JsonTo<>(int.class, Integer::valueOf, Number::intValue, b -> b ? 1 : 0));
    register(new JsonTo<>(long.class, Long::valueOf, Number::longValue, b -> b ? 1L : 0L));
    register(new JsonTo<>(float.class, Float::valueOf, Number::floatValue, b -> b ? 1f : 0f));
    register(new JsonTo<>(double.class, Double::valueOf, Number::doubleValue, b -> b ? 1d : 0d));
  }

  static final JsonMapping SHARED = AutoJsonMapper::createMapperCached;

  // TODO move cache to interface

  @SuppressWarnings("unchecked")
  private static <T> JsonTo<T> createMapperCached(Class<T> to) {
    return (JsonTo<T>) MAPPER_BY_TO_TYPE.computeIfAbsent(to, AutoJsonMapper::createMapper);
  }

  private static <T> JsonTo<T> createMapper(Class<T> to) {
    return new JsonTo<>(
        to, detect(String.class, to), detect(Number.class, to), detect(Boolean.class, to));
  }

  private static <A, B> Function<A, B> detect(Class<A> from, Class<B> to) {
    if (to.isEnum()) return mapToEnum(from, to);
    try {
      Constructor<B> c = to.getConstructor(from);
      return value -> {
        try {
          return c.newInstance(value);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      };
    } catch (NoSuchMethodException e) {
      return JsonMapping.unsupported(from, to);
    }
  }

  @SuppressWarnings("unchecked")
  private static <A, B> Function<A, B> mapToEnum(Class<A> from, Class<B> to) {
    B[] constants = to.getEnumConstants();
    if (from == String.class) return name -> (B) wrap(name, to);
    if (from == Number.class) return ordinal -> constants[((Number) ordinal).intValue()];
    return flag -> constants[flag == Boolean.FALSE ? 0 : 1];
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private static <E extends Enum<E>> Enum<?> wrap(Object from, Class to) {
    return Enum.valueOf(to, from.toString());
  }
}
