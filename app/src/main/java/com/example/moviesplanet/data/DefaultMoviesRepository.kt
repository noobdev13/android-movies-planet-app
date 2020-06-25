package com.example.moviesplanet.data

import com.example.moviesplanet.data.model.*
import com.example.moviesplanet.data.storage.remote.MoviesServiceApi
import com.example.moviesplanet.data.storage.local.AppPreferences
import com.example.moviesplanet.data.storage.local.db.MovieDao
import com.example.moviesplanet.data.storage.local.db.MovieEntity
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import java.lang.IllegalStateException

class DefaultMoviesRepository constructor(private val api: MoviesServiceApi,
                                          private val appPreferences: AppPreferences,
                                          private val movieDao: MovieDao) : MoviesRepository {

    /**
     * In-memory cache for list of [MovieGenre].
     */
    private var genres = listOf<MovieGenre>()

    override fun getMovies(page: Long): Single<List<Movie>> {
        return api.getMovies(appPreferences.getCurrentSortingOption().sortOption, page)
            .map { response ->
                response.results
                    ?.map { it.toMovie() }
                    ?:throw IllegalStateException()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getMovieDetails(movie: Movie): Single<MovieDetails> {
        return Single.zip(getMovieExternalInfo(movie), movieDao.getMovies().first(listOf()), getMovieGenres(movie), Function3<List<MovieExternalInfo>, List<MovieEntity>, List<MovieGenre>, MovieDetails> { t1, t2, t3 ->
                MovieDetails(movie, isFavoriteMovie(t2, movie.id), t1, t3)
        }).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun getMovieExternalInfo(movie: Movie): Single<List<MovieExternalInfo>> {
        val id = movie.id.toString()
        return getMovieReviewsExternalInfo(id)
            .zipWith(getMovieVideosExternalInfo(id),
                BiFunction<List<MovieExternalInfo>, List<MovieExternalInfo>,  List<List<MovieExternalInfo>>> { t1, t2 -> listOf(t1, t2) })
            .map { it.flatten() }
    }

    override fun setCurrentSortingOption(sortingOption: SortingOption) {
        appPreferences.setCurrentSortingOption(sortingOption)
    }

    override fun addToFavorite(movie: Movie): Completable {
        return movieDao.addMovie(MovieEntity(movie.id, movie.title, movie.releaseDate, movie.posterPath, movie.voteAverage, movie.overview))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun removeFromFavorite(movie: Movie): Completable {
        return movieDao.removeMovie(MovieEntity(movie.id, movie.title, movie.releaseDate, movie.posterPath, movie.voteAverage, movie.overview))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    override fun getFavoriteMovies(): Observable<List<Movie>> {
        return movieDao.getMovies()
            .map { favorites ->
                // TODO rework
                favorites.map { Movie(it.id, it.name, it.releaseDate, it.posterPath, it.voteAverage, it.overview, listOf()) }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    private fun getMovieReviewsExternalInfo(id: String): Single<List<MovieExternalInfo>> {
        return api.getMovieReviews(id)
            .map {
                it.results
                    ?.map { response -> response.toMovieExternalInfo() }
                    ?:throw IllegalStateException()
            }
    }

    private fun getMovieVideosExternalInfo(id: String): Single<List<MovieExternalInfo>> {
        return api.getMovieVideos(id)
            .map {
                it.results
                    ?.map { response -> response.toMovieExternalInfo() }
                    ?:throw IllegalStateException()
            }
    }

    private fun isFavoriteMovie(favorites: List<MovieEntity>, id: Int): Boolean {
        return favorites.find { it.id == id } != null
    }

    private fun getMovieGenres(movie: Movie): Single<List<MovieGenre>> {
        val source = if (genres.isEmpty()) getGenres() else Single.just(genres)
        return source.map {
            it.filter { genre -> movie.genres.contains(genre.id) }
        }
    }

    private fun getGenres(): Single<List<MovieGenre>> {
        return api.getMovieGenres()
            .map { response ->
                genres = response.genres?.map { MovieGenre(it.id, it.name) }?: listOf()
                genres
            }
    }
}