package com.xcape.movie_logger.imdbapi

import com.google.gson.GsonBuilder
import okhttp3.*
import java.io.IOException
import com.xcape.movie_logger.imdbapi.JsonObject.Companion.JSONSuggestedMovie
import com.xcape.movie_logger.imdbapi.JsonObject.Companion.SuggestedMovie
import com.xcape.movie_logger.imdbapi.JsonObject.Companion.JSONMovie
import com.xcape.movie_logger.imdbapi.JsonObject.Companion.MovieProduction

class ApiObject {
    companion object {
        fun searchMovie(
            movieKeyword: String,
            then: (jsonResponse: List<SuggestedMovie>) -> Unit
        ) {
            // Convert keyword to a url parameter
            val searchFor = movieKeyword.replace(" ", "_")

            // Get movie ids of possible movies based on the keyword in IMDB
            val IMDBSuggestionURL = "https://v2.sg.media-imdb.com/suggestion/${searchFor[0].lowercaseChar()}/${searchFor}.json"
            executeHttp(IMDBSuggestionURL, object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("Failed to execute request!\nError: ${e.localizedMessage}")
                }

                override fun onResponse(call: Call, response: Response) {
                    if(!response.isSuccessful)
                        throw IOException("Error response code: $response")

                    val body = response.body?.string()
                    val parsedJson = parseJson(body, JSONSuggestedMovie::class.java)
                    then(parsedJson.d)
                }
            })
        }

        fun getMovieInfo(
            movieId: String,
            then: (jsonResponse: JSONMovie) -> Unit
        ) {
            // Get movie info in IMDB
            val IMDBUrl = "https://www.imdb.com/title/${movieId}/"

            executeHttp(IMDBUrl, object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    println("Failed to execute request!\nError: ${e.localizedMessage}")
                }

                override fun onResponse(call: Call, response: Response) {
                    if(!response.isSuccessful)
                        throw IOException("Error response code: $response")

                    val body = response.body?.string()
                    val responseJson = getString(body.toString(), "<script type=\"application/ld+json\">", "<")

                    val parsedJson = parseJson(responseJson, JSONMovie::class.java)
                    then(parsedJson)
                }
            })
        }

        fun parseDate(date: String): String {
            val dateArray = date.split("-")
            val month = when(dateArray[1].toInt()) {
                1 -> "Jan"
                2 -> "Feb"
                3 -> "Mar"
                4 -> "Apr"
                5 -> "May"
                6 -> "Jun"
                7 -> "Jul"
                8 -> "Aug"
                9 -> "Sept"
                10 -> "Oct"
                11 -> "Nov"
                else -> "Dec"
            }

            // Concatenate back the dates
            val parsedDate = month + " " + dateArray[2] + ", " + dateArray[0]
            return parsedDate
        }

        fun parseDuration(duration: String?): String? {
            var newDuration = duration?.replace("PT", "")
            if(duration?.contains("H") == true) {
                val indexOfH = newDuration?.indexOf("H")

                // Insert a space after 'h' character
                if (indexOfH != null) {
                    newDuration = StringBuilder(newDuration)
                        .insert(indexOfH + 1, " ")
                        .toString()
                }
            }

            return newDuration?.lowercase()
        }

        fun parsePeople(people: List<MovieProduction>?): String? {
            if (people != null) {
                val listOfPeople: MutableList<String> = mutableListOf()
                for(person in people) {
                    listOfPeople.add(person.name)
                }
                return listOfPeople.joinToString(", ")
            }

            return null
        }

        private fun executeHttp(urlRequest: String, callback: Callback) {
            val request = Request.Builder()
                .url(urlRequest)
                .addHeader("Accept", "*/*")
                .addHeader("Connection", "keep-alive")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0; KTXN B657825059A118780T1297416P2) like Gecko")
                .build()

            val client = OkHttpClient()
            client.newCall(request).enqueue(callback)
        }

        private fun <T> parseJson(jsonString: String?, classJson: Class<T>): T {
            val gson = GsonBuilder().create()
            return gson.fromJson(jsonString, classJson)
        }

        private fun getString(str: String, delim1: String, delim2: String): String {
            val firstSplit = str.split(delim1)[1]
            val secondSplit = firstSplit.split(delim2)[0]

            return secondSplit
        }
    }
}