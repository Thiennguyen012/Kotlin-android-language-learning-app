package com.englishlearningapp.data.remote.dto.quiz

import com.google.gson.Gson
import org.junit.Assert.assertNull
import org.junit.Test

class CollectionTestDtoTest {

    @Test
    fun parsesQuizWithoutCollection() {
        val json = """
            {
              "id": 1,
              "test_type_id": 1,
              "collection_id": null,
              "test_name": "General English Test",
              "total_questions": 10,
              "duration": 15,
              "status": 1,
              "started_at": null,
              "finished_at": null,
              "test_type": {
                "id": 1,
                "type_name": "Multiple choice"
              },
              "collection": null,
              "created_at": "2026-06-30T00:00:00Z",
              "updated_at": "2026-06-30T00:00:00Z"
            }
        """.trimIndent()

        val quiz = Gson().fromJson(json, CollectionTestDto::class.java)

        assertNull(quiz.collectionId)
        assertNull(quiz.collection)
    }
}
