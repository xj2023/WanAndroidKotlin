package com.kx.kotlin.fragment

import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import android.view.View
import com.kx.kotlin.R
import com.kx.kotlin.adapter.ProjectPagerAdapter
import com.kx.kotlin.base.BaseFragment
import com.kx.kotlin.base.BaseObserver
import com.kx.kotlin.bean.ProjectTreeBean
import com.kx.kotlin.ext.showToast
import com.kx.kotlin.http.RetrofitHelper
import com.kx.kotlin.util.RxUtil
import kotlinx.android.synthetic.main.fragment_project.*

class ProjectFragment : BaseFragment() {

    companion object{
        fun getInstance() : ProjectFragment = ProjectFragment()
    }

    private var projectTree = mutableListOf<ProjectTreeBean>()


    override fun attachLayoutRes(): Int  = R.layout.fragment_project

    override fun initView() {
        viewPager.run {
            addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        }

        tabLayout.run {
            setupWithViewPager(viewPager)
            // TabLayoutHelper.setUpIndicatorWidth(tabLayout)
            addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))
            addOnTabSelectedListener(onTabSelectedListener)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getProjectTree()
    }
    private fun getProjectTree() {
        addDisposable(
            RetrofitHelper.service.getProjectTree().compose(RxUtil.ioMain())
                .subscribeWith(object : BaseObserver<List<ProjectTreeBean>>() {
                    override fun onSuccess(result: List<ProjectTreeBean>) {
                        projectTree.addAll(result)
                        viewPager.run {
                            adapter = viewPagerAdapter
                            offscreenPageLimit = result.size
                        }
                    }

                    override fun onError(errorMsg: String) {
                        showToast(errorMsg)
                    }
                }
                )
        )
    }

    private val onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(tab: TabLayout.Tab?) {
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) {
        }

        override fun onTabSelected(tab: TabLayout.Tab?) {
            // 默认切换的时候，会有一个过渡动画，设为false后，取消动画，直接显示
            tab?.let {
                viewPager.setCurrentItem(it.position, false)
            }
        }
    }

     fun scrollToTop() {
        if (viewPagerAdapter.count == 0) {
            return
        }
        val fragment: ProjectListFragment = viewPagerAdapter.getItem(viewPager.currentItem) as ProjectListFragment
        fragment.scrollToTop()
    }



    private val viewPagerAdapter: ProjectPagerAdapter by lazy {
        ProjectPagerAdapter(projectTree, childFragmentManager)
    }
}