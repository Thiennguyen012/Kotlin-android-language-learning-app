package com.vu.englishlearningapp.ui.screens.speaking

internal object PronunciationSentenceBank {
    private val sentences = listOf(
        "I usually take a short walk after dinner.",
        "My brother plays football with his friends every weekend.",
        "Could you tell me where the nearest bus stop is?",
        "We are planning a small picnic for Sunday afternoon.",
        "She always drinks a glass of water in the morning.",
        "I need to buy some vegetables on my way home.",
        "The weather is warm enough to eat outside today.",
        "My favorite cafe serves fresh bread and hot coffee.",
        "Please remember to turn off the lights before you leave.",
        "I often listen to music while I am cooking dinner.",
        "They moved to a new apartment near the city center.",
        "Would you like to watch a movie with us tonight?",
        "The train was late, so we arrived after lunch.",
        "I am saving money because I want to travel next summer.",
        "Our teacher gave us some useful advice for the exam.",
        "This restaurant is cheaper than the one across the street.",
        "I have lived in this neighborhood for three years.",
        "If I have enough time, I will visit the museum.",
        "We should leave early to avoid the heavy traffic.",
        "She was reading a book when her friend called.",
        "The hotel room was clean, comfortable, and surprisingly quiet.",
        "I enjoy learning English because it helps me meet new people.",
        "Although it was raining, we still enjoyed our day trip.",
        "The book I borrowed was more interesting than I expected.",
        "He decided to take the stairs instead of the elevator.",
        "Working from home can save both time and money.",
        "I forgot my umbrella, but luckily the rain stopped soon.",
        "There are several ways to improve your speaking skills.",
        "My parents taught me how to cook this simple meal.",
        "We had to change our plans because the shop was closed.",
        "I would like to learn more about different cultures.",
        "The park is a peaceful place to relax after work.",
        "Before going to bed, I prepare everything for the next day.",
        "Public transport is convenient when the roads are busy.",
        "She speaks slowly so that everyone can understand her.",
        "I think regular exercise is important for a healthy life."
    )

    fun random(excluding: String? = null): String {
        val choices = sentences.filterNot { it == excluding }
        return (choices.ifEmpty { sentences }).random()
    }
}
