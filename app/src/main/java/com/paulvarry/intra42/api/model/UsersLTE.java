package com.paulvarry.intra42.api.model;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.paulvarry.intra42.AppClass;
import com.paulvarry.intra42.activities.user.UserActivity;
import com.paulvarry.intra42.api.IBaseItemSmall;
import com.paulvarry.intra42.api.ServiceGenerator;
import com.paulvarry.intra42.cache.BaseCacheData;
import org.parceler.Parcel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Parcel
public class UsersLTE
        extends BaseCacheData
        implements IBaseItemSmall, Comparable<UsersLTE> {

    final static String API_ID = "id";
    final static String API_LOGIN = "login";
    final static String API_URL = "url";

    @SerializedName(API_ID)
    public int id;
    @SerializedName(API_LOGIN)
    public String login;
    @SerializedName(API_URL)
    public String url;

    public static Type getListType() {
        return new TypeToken<List<UsersLTE>>() {
        }.getType();
    }

    public static String concatIds(List<UsersLTE> list) {
        if (list != null)
            return concatIds(list, 0, list.size());
        return null;
    }

    public static String concatIds(List<UsersLTE> list, int start, int size) {
        String eventsId;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            StringJoiner join = new StringJoiner(",");
            for (int i = 0; i < size; i++) {
                if (list.size() > start + i)
                    join.add(String.valueOf(list.get(start + i).id));
            }
            eventsId = join.toString();
        } else {
            StringBuilder builder = new StringBuilder();
            String join = "";
            for (int i = 0; i < size; i++) {
                if (list.size() > start + i)
                    builder.append(join).append(String.valueOf(list.get(start + i).id));
                join = ",";
            }
            eventsId = builder.toString();
        }

        return eventsId;
    }

    public boolean equals(UsersLTE user) {
        return user != null && user.id == id;
    }

    public boolean isMe(AppClass app) {
        return equals(app.me);
    }

    @Override
    public String getName(Context context) {
        return login;
    }

    @Override
    public String getSub(Context context) {
        return null;
    }

    @Override
    public boolean openIt(Context context) {
        UserActivity.openIt(context, this);
        return true;
    }

    @Override
    public int compareTo(@NonNull UsersLTE o) {
        return login.compareTo(o.login);
    }

    @Override
    public int getId() {
        return id;
    }

    static public class UserLTEDeserializer implements JsonDeserializer<UsersLTE> {

        @Override
        public UsersLTE deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            if (!json.isJsonObject() || json.isJsonNull())
                return null;

            UsersLTE user = new UsersLTE();
            JsonObject jsonObject = json.getAsJsonObject();
            Gson gson = ServiceGenerator.getGson();

            if (jsonObject.size() == 0)
                return null;

            user.id = jsonObject.get(API_ID).getAsInt();
            user.login = gson.fromJson(jsonObject.get(API_LOGIN), String.class);
            user.url = gson.fromJson(jsonObject.get(API_URL), String.class);

            return user;
        }
    }

    static public class ListUserLTEDeserializer implements JsonDeserializer<List<UsersLTE>> {

        @Override
        public List<UsersLTE> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            if (!json.isJsonArray() || json.isJsonNull())
                return null;

            List<UsersLTE> list = new ArrayList<>();
            JsonArray jsonObject = json.getAsJsonArray();
            Gson gson = ServiceGenerator.getGson();

            for (int i = 0; i < jsonObject.size(); i++) {
                list.add(gson.fromJson(jsonObject.get(i), UsersLTE.class));
            }

            return list;
        }
    }
}
