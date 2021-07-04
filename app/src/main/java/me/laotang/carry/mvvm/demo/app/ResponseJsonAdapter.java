package me.laotang.carry.mvvm.demo.app;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.ToJson;
import com.squareup.moshi.Types;
import com.squareup.moshi.internal.Util;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Set;

import javax.annotation.Nullable;

import me.laotang.carry.core.http.response.BaseResponseBean;

public final class ResponseJsonAdapter<T> extends JsonAdapter<BaseResponseBean<T>> {

    public static final Factory FACTORY = new Factory() {
        @Nullable
        @Override
        public JsonAdapter<?> create(Type type, Set<? extends Annotation> annotations, Moshi moshi) {
            if (!annotations.isEmpty()) return null;
            Class<?> rawType = Types.getRawType(type);
            if (rawType != BaseResponseBean.class) return null;
            if (type instanceof ParameterizedType) {
                Type elementType = getParameterUpperBound(0, (ParameterizedType) type);
                return new ResponseJsonAdapter<>(moshi, elementType).nullSafe();
            }
            return null;
        }
    };

    private final JsonAdapter<T> elementAdapter;
    private final JsonAdapter<Integer> integerAdapter;
    private final JsonAdapter<String> stringAdapter;

    private JsonReader.Options options = JsonReader.Options.of("code", "message", "data");

    private ResponseJsonAdapter(Moshi moshi, Type keyType) {
        this.elementAdapter = moshi.adapter(keyType);
        this.integerAdapter = moshi.adapter(Integer.class, Util.NO_ANNOTATIONS, "code");
        this.stringAdapter = moshi.adapter(String.class, Util.NO_ANNOTATIONS, "message");
    }

    static Type getParameterUpperBound(int index, ParameterizedType type) {
        Type[] types = type.getActualTypeArguments();
        if (index < 0 || index >= types.length) {
            throw new IllegalArgumentException(
                    "Index " + index + " not in range [0," + types.length + ") for " + type);
        }
        Type paramType = types[index];
        if (paramType instanceof WildcardType) {
            return ((WildcardType) paramType).getUpperBounds()[0];
        }
        return paramType;
    }

    @Nullable
    @Override
    @FromJson
    public BaseResponseBean<T> fromJson(JsonReader reader) throws IOException {
        int code = 0;
        String message = "";
        T data = null;
        BaseResponseBean<T> bean = new BaseResponseBean<>();
        reader.beginObject();
        while (reader.hasNext()) {
            int index = reader.selectName(options);
            if (index == -1) {
                reader.skipName();
                reader.skipValue();
                continue;
            }
            if (index == 0) {
                Integer tempCode = integerAdapter.nullSafe().fromJson(reader);
                if (tempCode != null) {
                    code = tempCode;
                }
            } else if (index == 1) {
                String tempMessage = stringAdapter.fromJson(reader);
                if (tempMessage != null) {
                    message = tempMessage;
                }
            } else if (index == 2) {
                data = elementAdapter.nullSafe().fromJson(reader);
            }
        }
        reader.endObject();
        bean.setCode(code);
        bean.setMessage(message);
        bean.setData(data);
        bean.setSuccess(code == 200);
        return bean;
    }

    @Override
    @ToJson
    public void toJson(JsonWriter writer, @Nullable BaseResponseBean<T> value) throws IOException {
        if (value == null) {
            throw new NullPointerException("value_ was null! Wrap in .nullSafe() to write nullable values.");
        }
        writer.beginObject();
        writer.name("code");
        integerAdapter.toJson(writer, value.getCode());
        writer.name("message");
        stringAdapter.toJson(writer, value.getMessage());
        writer.name("data");
        elementAdapter.toJson(writer, value.getData());
        writer.endObject();
    }

    @Override
    public String toString() {
        return "JsonAdapter(BaseResponseBean<" + elementAdapter + ">)";
    }
}
