package ru.kuchanov.scpreaderapi.bean.firebase;

import com.google.firebase.database.PropertyName;

import java.util.Map;

/**
 * Created by mohax on 26.03.2017.
 * <p>
 * for scp_ru
 */
public class ArticleInFirebase {

    public boolean isFavorite;
    public boolean isRead;
    public String title;
    public String url;
    public long updated;

    public Object tag;
    public Object offset;

    @PropertyName("c-192651")
    public Map<String, Object> first;
    @PropertyName("t-2531704")
    public Map<String, Object> se;
    @PropertyName("t-3475761")
    public Map<String, Object> th;
    @PropertyName("t-2309422")
    public Map<String, Object> fo;
    @PropertyName("t-3590480")
    public Map<String, Object> fi;
    @PropertyName("forum")
    public Map<String, Object> forum;
    @PropertyName("t-1618412")
    public Map<String, Object> forum0;


    @PropertyName("t-843093")
    public Map<String, Object> forum1;
    @PropertyName("t-149088")
    public Map<String, Object> forum2;
    @PropertyName("t-443377")
    public Map<String, Object> forum3;

    @PropertyName("t-730164")
    public Map<String, Object> forum4;
    @PropertyName("t-1578716")
    public Map<String, Object> forum5;


    @PropertyName("p")
    public Map<String, Object> forum6;
    @PropertyName("t-607800")
    public Map<String, Object> forum7;
    @PropertyName("binge_order")
    public Map<String, Object> forum8;

    public ArticleInFirebase(boolean isFavorite, boolean isRead, String title, String url, long updated) {
        this.isFavorite = isFavorite;
        this.isRead = isRead;
        this.title = title;
        this.url = url;
        this.updated = updated;
    }

    public ArticleInFirebase() {
    }

    @Override
    public String toString() {
        return "ArticleInFirebase{" +
                "isFavorite=" + isFavorite +
                ", isRead=" + isRead +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", updated=" + updated +
                '}';
    }
}