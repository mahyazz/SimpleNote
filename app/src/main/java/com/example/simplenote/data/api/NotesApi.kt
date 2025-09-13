package com.example.simplenote.data.api

import com.example.simplenote.data.api.model.NoteCreateUpdateBody
import com.example.simplenote.data.api.model.NoteDto
import com.example.simplenote.data.api.model.PageDto
import retrofit2.Response
import retrofit2.http.*

data class NotePatchBody(val title: String? = null, val description: String? = null)

interface NotesApi {

    // معمولی
    @GET("/api/notes/")
    suspend fun listNotes(
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null
    ): PageDto<NoteDto>

    // برای دیباگ: با Response تا کد وضعیت را ببینیم
    @GET("/api/notes/")
    suspend fun listNotesResponse(
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null
    ): Response<PageDto<NoteDto>>

    @GET("/api/notes/filter")
    suspend fun filterNotes(
        @Query("updated__gte") updatedGte: String?,
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null,
        @Query("title") title: String? = null,
        @Query("description") description: String? = null
    ): PageDto<NoteDto>

    @POST("/api/notes/") suspend fun createNote(@Body body: NoteCreateUpdateBody): NoteDto

    @GET("/api/notes/{id}/") suspend fun retrieveNote(@Path("id") id: Int): NoteDto
    @PUT("/api/notes/{id}/") suspend fun updateNote(@Path("id") id: Int, @Body body: NoteCreateUpdateBody): NoteDto
    @PATCH("/api/notes/{id}/") suspend fun partialUpdateNote(@Path("id") id: Int, @Body body: NotePatchBody): NoteDto
    @DELETE("/api/notes/{id}/") suspend fun deleteNote(@Path("id") id: Int): Response<Unit>

    @POST("/api/notes/bulk") suspend fun bulkCreate(@Body body: List<NoteCreateUpdateBody>): List<NoteDto>
}
