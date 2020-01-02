package ru.boomik.vrnbus.dal;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

  private Map<String, Interceptor> apiAuthorizations;
  private OkHttpClient.Builder okBuilder;
  private Retrofit.Builder adapterBuilder;

  public ApiClient() {
    apiAuthorizations = new LinkedHashMap<>();
    createDefaultAdapter();
  }

  public void createDefaultAdapter() {
    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
    logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

    okBuilder = new OkHttpClient.Builder()
            .addInterceptor(logging);

    String baseUrl = Consts.SERVER_URL;

    adapterBuilder = new Retrofit
            .Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create());
  }

  public <S> S createService(Class<S> serviceClass) {
    return adapterBuilder
            .client(okBuilder.build())
            .build()
            .create(serviceClass);
  }

  /**
   * This wrapper is to take care of this case:
   * when the deserialization fails due to JsonParseException and the
   * expected type is String, then just return the body string.
   */
  class GsonResponseBodyConverterToString<T> implements Converter<ResponseBody, T> {
    private final Gson gson;
    private final Type type;

    GsonResponseBodyConverterToString(Gson gson, Type type) {
      this.gson = gson;
      this.type = type;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
      String returned = value.string();
      try {
        return gson.fromJson(returned, type);
      } catch (JsonParseException e) {
        return (T) returned;
      }
    }
  }
}