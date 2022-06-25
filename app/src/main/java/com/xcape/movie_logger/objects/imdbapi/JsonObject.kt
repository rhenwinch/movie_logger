package com.xcape.movie_logger.objects.imdbapi

class JsonObject {
    companion object {
        //== Suggestion Movies JSON Object ==\\

        class JSONSuggestedMovie(val d: List<SuggestedMovie>)
            // l is for title in the JSON body
            class SuggestedMovie(
                val id: String,
                val l: String,
                val i: SuggestedMovieImage
            )
                class SuggestedMovieImage(val imageUrl: String)

        //== End of Suggestion Movies JSON Object ==\\




        //== Movie Information JSON Object ==\\
        class JSONMovie(
            val name: String,
            val description: String,
            val datePublished: String,
            val image: String,
            val aggregateRating: MovieRating,
            val contentRating: String,
            val genre: List<String>,
            val trailer: MovieTrailer,
            val duration: String?,
            val actor: List<MovieProduction>,
            val director: List<MovieProduction>?,
            val creator: List<MovieProduction>?,
            var id: String,
        )
            class MovieProduction(val name: String)
            class MovieRating(val ratingCount: Int, val ratingValue: Float)
            class MovieTrailer(val thumbnailUrl: String)

        //== Movie Information JSON Object ==\\
    }
}