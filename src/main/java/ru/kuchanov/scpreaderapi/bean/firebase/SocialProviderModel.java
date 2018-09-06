package ru.kuchanov.scpreaderapi.bean.firebase;

import com.google.gson.annotations.Expose;

/**
 * Created by mohax on 25.03.2017.
 * <p>
 * for scp_ru
 */
public class SocialProviderModel {

    @Expose
    public String provider;

    @Expose
    public String id;

    public boolean managed;
    public boolean valid;
    public boolean loaded;

    public SocialProviderModel(String provider, String id) {
        this.provider = provider;
        this.id = id;
    }

    public SocialProviderModel() {
    }

//    public static SocialProviderModel getSocialProviderModelForProvider(Constants.Firebase.SocialProvider provider){
//        switch (provider){
//            case VK:
//                return new SocialProviderModel(provider.name(), VKAccessToken.currentToken().userId);
//            default:
//                throw new IllegalArgumentException("unexpected provider");
//        }
//    }

    @Override
    public String toString() {
        return "SocialProviderModel{" +
                "provider='" + provider + '\'' +
                ", id=" + id +
                '}';
    }
}