package com.example.moviesplanet.presentation.movies

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.example.moviesplanet.data.MoviesDataSourceFactory
import com.example.moviesplanet.data.MoviesRepository
import com.example.moviesplanet.data.PagingLoadingStatus
import com.example.moviesplanet.data.model.Movie
import com.example.moviesplanet.data.model.SortingOption
import com.example.moviesplanet.presentation.generic.LiveDataEvent
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class MoviesViewModel @Inject constructor(private val moviesRepository: MoviesRepository,
                                          movieDataSourceFactory: MoviesDataSourceFactory) : ViewModel() {

    private val _moviesLiveData: LiveData<PagedList<Movie>>
    val moviesLiveData: LiveData<PagedList<Movie>>
        get() = _moviesLiveData

    private val _moviesLoadingStatusLiveData = Transformations.switchMap(movieDataSourceFactory.repositoryDataSourceLiveData) {
        it.loadingStatusLiveData
    }
    val moviesLoadingStatusLiveData: LiveData<PagingLoadingStatus>
        get() = _moviesLoadingStatusLiveData

    val firstLoadFailedLiveData = MutableLiveData<Boolean>()

    val loadFailedLiveData = MutableLiveData<LiveDataEvent<String?>>()

    val loadingIndicatorLiveData = MutableLiveData<Boolean>()

    val navigateToDetailsLiveData = MutableLiveData<LiveDataEvent<Movie>>()

    // TODO make enum
    val navigateToMyFavoritesLiveData = MutableLiveData<LiveDataEvent<Boolean>>()

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    //private val movies = mutableListOf<Movie>()

    //private var currentPage: Int = 1

    init {
        //loadMovies()
        val config = PagedList.Config.Builder()
            .setPageSize(PAGE_SIZE)
            .build()
        _moviesLiveData = LivePagedListBuilder<Long, Movie>(movieDataSourceFactory, config).build()
    }

    //fun loadMoreMovies() {
    //    loadMovies()
    //}

    fun tryAgainClick() {
        loadMovies()
    }

    fun sortByPopularClick() {
        moviesRepository.setCurrentSortingOption(SortingOption.POPULAR)
        // Reset page.
        //currentPage = 1
        // Remove all previous data.
        resetMovies()
        loadMovies()
    }

    fun sortByTopRatedClick() {
        moviesRepository.setCurrentSortingOption(SortingOption.TOP_RATED)
        // Reset page.
        //currentPage = 1
        // Remove all previous data.
        resetMovies()
        loadMovies()
    }

    fun onMyFavoritesClick() {
        navigateToMyFavoritesLiveData.value = LiveDataEvent(true)
    }

    fun onMovieClick(movie: Movie) {
        navigateToDetailsLiveData.value = LiveDataEvent(movie)
    }

    private fun loadMovies() {
        firstLoadFailedLiveData.value = false
        //if (currentPage == 1) {
        //    loadingIndicatorLiveData.value = true
        //}

        //val disposable = moviesRepository.getMovies(currentPage).subscribe({
        //    onLoadSuccessful(it)
        //}, {
        //     onLoadFailed(it)
        //})
        //compositeDisposable.add(disposable)
    }

    private fun resetMovies() {
        //movies.clear()
        //moviesLiveData.value = movies
    }

    private fun onLoadSuccessful(list: List<Movie>) {
        // Update page num.
        //currentPage++
        // If list is empty, we reach the end of list, so do nothing. Else, update UI.
        if (list.isNotEmpty()) {
            //movies.addAll(list)
            //moviesLiveData.value = movies
        }
        // Hide loading indicator.
        loadingIndicatorLiveData.value = false
    }

    private fun onLoadFailed(throwable: Throwable) {
        Log.d(KEY_LOG, throwable.message)
        //if (currentPage == 1) {
        //    // First load has been failed.
        //    firstLoadFailedLiveData.value = true
        //} else {
        //    loadFailedLiveData.value = LiveDataEvent(throwable.message)
        //}
        // Hide loading indicator.
        loadingIndicatorLiveData.value = false
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    companion object {
        private const val KEY_LOG = "Movies_view_model"
        private const val PAGE_SIZE = 20
    }
}