package ru.kuchanov.scpreaderapi.model.dto.article;

import org.springframework.beans.factory.annotation.Value;
import ru.kuchanov.scpreaderapi.bean.articles.ArticleForLang;
import ru.kuchanov.scpreaderapi.bean.articles.ArticlesImages;

import java.util.List;
import java.util.Set;

//@Projection(
//        name = "customBook",
//        types = { ArticleForLang.class }
//        )
public interface ArticleInListWithImages {

    @Value("#{target.article_id}")
    Long getArticleId();

    @Value("#{target.lang_id}")
    String getLangId();

    @Value("#{target.url_relative}")
    String getUrlRelative();

    String getTitle();

    Integer getRating();

    ////    @Value("#{target.images}")
//    val images: List<ArticlesImages>

//    @Value("#{target.images}")
    Set<ArticlesImages> getImages();



}
