package com.kx.kotlin.adapter

import android.content.Context
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.kx.kotlin.R
import com.kx.kotlin.bean.Article
import com.kx.kotlin.util.ImageLoader

class KnowledgeAdapter(private val context: Context?)
    : BaseQuickAdapter<Article, BaseViewHolder>(R.layout.item_knowledge_list) {

    override fun convert(helper: BaseViewHolder, item: Article?) {
        item ?: return
        helper ?: return
        helper.setText(R.id.tv_article_title, Html.fromHtml(item.title))
                .setText(R.id.tv_article_author, item.author)
                .setText(R.id.tv_article_date, item.niceDate)
                .setImageResource(R.id.iv_like,
                        if (item.collect) R.drawable.ic_like else R.drawable.ic_like_not
                )
                .addOnClickListener(R.id.iv_like)
        val chapterName = when {
            item.superChapterName.isNotEmpty() and item.chapterName.isNotEmpty() ->
                "${item.superChapterName} / ${item.chapterName}"
            item.superChapterName.isNotEmpty() -> item.superChapterName
            item.chapterName.isNotEmpty() -> item.chapterName
            else -> ""
        }
        helper.setText(R.id.tv_article_chapterName, chapterName)

        if (!TextUtils.isEmpty(item.envelopePic)) {
            helper.getView<ImageView>(R.id.iv_article_thumbnail)
                    .visibility = View.VISIBLE
            context?.let {
                ImageLoader.load(it, item.envelopePic, helper.getView(R.id.iv_article_thumbnail))
            }
        } else {
            helper.getView<ImageView>(R.id.iv_article_thumbnail)
                    .visibility = View.GONE
        }
    }

}