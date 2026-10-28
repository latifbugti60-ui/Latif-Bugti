package com.example.data.model

data class SampleScript(
    val title: String,
    val category: String,
    val text: String,
    val recommendedVoiceId: String
)

object ScriptPresets {
    val samples: List<SampleScript> = listOf(
        SampleScript(
            title = "Roman Urdu Kahani",
            category = "Roman Urdu",
            text = "Ek dafa ka zikr hai, ek khoobsurat wadi mein shaam ka sooraj dhal raha tha. Door paharon ke peeche parinday apne ghonslon ki taraf laut rahe thay. Hawa mein mitti ki sondhi khushboo rachi hui thi, jaise kisi purane zamane ki daastaan yaad dila rahi ho.",
            recommendedVoiceId = "hamza_urdu"
        ),
        SampleScript(
            title = "Roman Urdu Shayari",
            category = "Roman Urdu",
            text = "Sitaron se aage jahan aur bhi hain,\nAbhi ishq ke imtehan aur bhi hain.\nQana'at na kar aalam-e-rang-o-boo par,\nChaman aur bhi, aashiyan aur bhi hain.",
            recommendedVoiceId = "zara_urdu"
        ),
        SampleScript(
            title = "Roman Urdu Tech / YouTube",
            category = "Roman Urdu",
            text = "Dosto, aaj ki video mein hum baat karne ja rahe hain ek aise AI voice generator ke baare mein jo bilkul ElevenLabs ki tarah studio quality aawaz generate karta hai. Video ko like karein aur channel ko zaroor subscribe karein!",
            recommendedVoiceId = "ali_roman"
        ),
        SampleScript(
            title = "Roman Urdu Motivation",
            category = "Roman Urdu",
            text = "Zindagi mein kabhi bhi haar mat mano. Har mushkil waqt ek naya sabaq le kar aata hai. Tumhari mehnat aur tumhara hausla hi tumhe kamyabi tak pohanchaye ga.",
            recommendedVoiceId = "hamza_urdu"
        ),
        SampleScript(
            title = "اردو کہانی (Urdu Tale)",
            category = "Urdu Script",
            text = "ایک خوبصورت وادی میں شام کا سورج ڈھل رہا تھا۔ دور پہاڑوں کے پیچھے پرندوں کی ڈاریں اپنے گھونسلوں کی طرف لوٹ رہی تھیں۔ ہوا میں مٹی کی سوندھی خوشبو رچی ہوئی تھی، جیسے پرانے وقتوں کی کوئی بھولی بسری داستان یاد دلا رہی ہو۔",
            recommendedVoiceId = "hamza_urdu"
        ),
        SampleScript(
            title = "اردو شاعری (Urdu Poetry)",
            category = "Urdu Script",
            text = "ستاروں سے آگے جہاں اور بھی ہیں،\nابھی عشق کے امتحاں اور بھی ہیں۔\nقناعت نہ کر عالمِ رنگ و بو پر،\nچمن اور بھی، آشیاں اور بھی ہیں۔",
            recommendedVoiceId = "zara_urdu"
        ),
        SampleScript(
            title = "AI Voice Intro",
            category = "Narration",
            text = "In an era of limitless digital creation, artificial intelligence transforms simple written thoughts into lifelike human speech. Every syllable carries resonance, intention, and breath.",
            recommendedVoiceId = "adam"
        ),
        SampleScript(
            title = "Cinematic Movie Trailer",
            category = "Cinematic",
            text = "They told us the signal was lost ten thousand light years away. They were wrong. It didn't fade... it evolved. This summer, prepare to face what was waiting in the dark.",
            recommendedVoiceId = "marcus"
        ),
        SampleScript(
            title = "Calm Guided Meditation",
            category = "Conversational",
            text = "Welcome to your sanctuary of stillness. Relax your shoulders. Inhale deeply through your nose, letting calmness fill your mind, and gently exhale any tension away.",
            recommendedVoiceId = "rachel"
        )
    )
}
