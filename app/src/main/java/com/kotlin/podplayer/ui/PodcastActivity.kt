package com.kotlin.podplayer.ui

import android.animation.ValueAnimator
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.kotlin.podplayer.R
import com.kotlin.podplayer.adapter.PodcastListAdapter
import com.kotlin.podplayer.adapter.PodcastListAdapter.PodcastListAdapterListener
import com.kotlin.podplayer.databinding.ActivityPodcastBinding
import com.kotlin.podplayer.ui.PodcastDetailsFragment.OnPodcastDetailsListener
import com.kotlin.podplayer.viewmodel.PodcastViewModel
import com.kotlin.podplayer.viewmodel.SearchViewModel
import com.kotlin.podplayer.worker.EpisodeUpdateWorker
import com.facebook.shimmer.Shimmer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import org.koin.androidx.viewmodel.ext.android.viewModel

class PodcastActivity : AppCompatActivity(), PodcastListAdapterListener, OnPodcastDetailsListener {

    private val searchViewModel: SearchViewModel by viewModel()
    private val podcastViewModel: PodcastViewModel by viewModel()

    private lateinit var podcastListAdapter: PodcastListAdapter
    private lateinit var searchMenuItem: MenuItem
    private lateinit var databinding: ActivityPodcastBinding
    private var isEnabledToShowSubscribed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databinding = ActivityPodcastBinding.inflate(layoutInflater)
        setContentView(databinding.root)
        setupToolbar()
        setupViewModels()
        updateControls()
        setupPodcastListView()
        handleIntent(intent)
        addBackStackListener()
        scheduleJobs()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_search, menu)

        searchMenuItem = menu.findItem(R.id.search_item)
        val searchView = searchMenuItem.actionView as SearchView

        searchMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                showSubscribedPodcasts()
                return true
            }
        })

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))

        if (supportFragmentManager.backStackEntryCount > 0) {
            databinding.podcastRecyclerView.visibility = View.INVISIBLE
        }

        if (databinding.podcastRecyclerView.visibility == View.INVISIBLE) {
            searchMenuItem.isVisible = false
        }
        return true
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onShowDetails(podcastSummaryViewData: SearchViewModel.PodcastSummaryViewData) {
        podcastSummaryViewData.feedUrl ?: return
        showProgressBar()
        podcastViewModel.viewModelScope.launch(context = Dispatchers.Main) {
            podcastViewModel.getPodcast(podcastSummaryViewData)
            hideProgressBar()
            showDetailsFragment()
        }
    }

    private fun scheduleJobs() {
        val constraints: Constraints = Constraints.Builder().apply {
            setRequiredNetworkType(NetworkType.CONNECTED)
            setRequiresCharging(true)
        }.build()

        val request = PeriodicWorkRequestBuilder<EpisodeUpdateWorker>(
                1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(TAG_EPISODE_UPDATE_JOB,
                ExistingPeriodicWorkPolicy.REPLACE, request)
    }

    private fun showSubscribedPodcasts() {
        val podcasts = podcastViewModel.getPodcasts()?.value

        if (podcasts != null) {
            databinding.toolbar.title = getString(R.string.subscribed_podcasts)
            podcastListAdapter.setSearchData(podcasts)
        }
    }

    override fun onBackPressed() {
        if (isEnabledToShowSubscribed) {
            showSubscribedPodcasts()
            isEnabledToShowSubscribed = false
        } else {
            super.onBackPressed()
        }
    }

    private fun addBackStackListener() {
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                databinding.podcastRecyclerView.visibility = View.VISIBLE
                isEnabledToShowSubscribed = true
            }
        }
    }

    private fun performSearch(term: String) {
        showProgressBar()
        GlobalScope.launch {
            val results = searchViewModel.searchPodcasts(term)
            showShimmer()

            if (results.isNotEmpty()) {
                withContext(Dispatchers.Main) {
                    hideProgressBar()
                    stopShimmer()
                    databinding.toolbar.title = term
                    podcastListAdapter.setSearchData(results)
                }
            } else {
                withContext(Dispatchers.Main) {
                    hideProgressBar()
                    Toast.makeText(
                            this@PodcastActivity,
                            "Result were not found by $term",
                            Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY) ?: return
            performSearch(query)
        }
        val podcastFeedUrl = intent.getStringExtra(EpisodeUpdateWorker.EXTRA_FEED_URL)
        if (podcastFeedUrl != null) {
            podcastViewModel.viewModelScope.launch {
                val podcastSummaryViewData = podcastViewModel.setActivePodcast(podcastFeedUrl)
                podcastSummaryViewData?.let { podcastSummaryView -> onShowDetails(podcastSummaryView) }
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(databinding.toolbar)
    }

    private fun setupViewModels() {
        searchViewModel
    }

    private fun setupPodcastListView() {
        podcastViewModel.getPodcasts()?.observe(this, {
            if (it != null) {
                showSubscribedPodcasts()
            }
        })
    }

    private fun updateControls() {
        databinding.podcastRecyclerView.setHasFixedSize(true)

        val layoutManager = LinearLayoutManager(this)
        databinding.podcastRecyclerView.layoutManager = layoutManager

        val dividerItemDecoration = DividerItemDecoration(
                databinding.podcastRecyclerView.context,
                layoutManager.orientation)
        databinding.podcastRecyclerView.addItemDecoration(dividerItemDecoration)

        podcastListAdapter = PodcastListAdapter(null, this, this)
        databinding.podcastRecyclerView.adapter = podcastListAdapter

        initAndStartShimmer()
        stopShimmer()
    }

    private fun stopShimmer(){
        Handler().postDelayed({
            databinding.shimmerViewContainer.stopShimmer()
            databinding.shimmerViewContainer.hideShimmer()
        }, 2500)
    }

    private fun showShimmer() {
        databinding.shimmerViewContainer.startShimmer()
        databinding.shimmerViewContainer.showShimmer(true)
    }

    private fun initAndStartShimmer() {
        val shimmerBuilder = Shimmer.AlphaHighlightBuilder()
        databinding.shimmerViewContainer.setShimmer(
                shimmerBuilder.setDuration(2000L)
                        .setRepeatMode(ValueAnimator.REVERSE).build())
    }

    private fun showDetailsFragment() {
        val podcastDetailsFragment = createPodcastDetailsFragment()

        val f = supportFragmentManager.beginTransaction()
        f.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
        f.add(R.id.podcastDetailsContainer, podcastDetailsFragment, TAG_DETAILS_FRAGMENT)
        f.addToBackStack("PlayerFragment").commit()

        databinding.podcastRecyclerView.visibility = View.INVISIBLE
        searchMenuItem.isVisible = false
    }

    private fun showPlayerFragment() {
        val episodePlayerFragment = createEpisodePlayerFragment()

        val f = supportFragmentManager.beginTransaction()
        f.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit)
        f.replace(R.id.podcastDetailsContainer, episodePlayerFragment, TAG_PLAYER_FRAGMENT)
        f.addToBackStack("PlayerFragment").commit()

        databinding.podcastRecyclerView.visibility = View.INVISIBLE
        searchMenuItem.isVisible = false
    }

    private fun createEpisodePlayerFragment(): EpisodePlayerFragment {
        var episodePlayerFragment = supportFragmentManager.findFragmentByTag(TAG_PLAYER_FRAGMENT) as
                EpisodePlayerFragment?

        if (episodePlayerFragment == null) {
            episodePlayerFragment = EpisodePlayerFragment.newInstance()
        }
        return episodePlayerFragment
    }

    private fun createPodcastDetailsFragment(): PodcastDetailsFragment {
        var podcastDetailsFragment = supportFragmentManager.findFragmentByTag(TAG_DETAILS_FRAGMENT) as PodcastDetailsFragment?
        if (podcastDetailsFragment == null) {
            podcastDetailsFragment = PodcastDetailsFragment.newInstance()
        }
        return podcastDetailsFragment
    }

    private fun showProgressBar() {
        databinding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        databinding.progressBar.visibility = View.INVISIBLE
    }

    companion object {
        private const val TAG_DETAILS_FRAGMENT = "DetailsFragment"
        private const val TAG_EPISODE_UPDATE_JOB = "com.raywenderlich.podplay.episodes"
        private const val TAG_PLAYER_FRAGMENT = "PlayerFragment"
    }

    override fun onSubscribe() {
        podcastViewModel.saveActivePodcast()
        supportFragmentManager.popBackStack()
    }

    override fun onUnsubscribe() {
        podcastViewModel.deleteActivePodcast()
        supportFragmentManager.popBackStack()
    }

    override fun onShowEpisodePlayer(episodeViewData: PodcastViewModel.EpisodeViewData) {
        podcastViewModel.activeEpisodeViewData = episodeViewData
        showPlayerFragment()
    }
}
