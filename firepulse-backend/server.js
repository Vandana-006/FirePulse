require('dotenv').config();
const express = require('express');
const cors = require('cors');
const { GoogleGenAI } = require('@google/genai');

const app = express();
app.use(cors());
app.use(express.json());

const ai = new GoogleGenAI({ apiKey: process.env.GEMINI_API_KEY });

// Route to handle scene questions from Fire TV
app.post('/api/ask', async (req, res) => {
    try {
        const { question, sceneContext } = req.body;

        const prompt = `You are FirePulse, an AI assistant for Fire TV watching a video. 
Current Scene Context: ${sceneContext || "User is currently watching a scene."}
User Question: ${question}

Provide a concise, engaging answer suitable for a TV screen UI.`;

        const response = await ai.models.generateContent({
            model: 'gemini-2.5-flash',
            contents: prompt,
        });

        res.json({ success: true, answer: response.text });
    } catch (error) {
        console.error("Gemini API Error:", error);
        res.status(500).json({ success: false, error: "Failed to fetch response from Gemini." });
    }
});

// Route to generate interactive quizzes
app.post('/api/quiz', async (req, res) => {
    try {
        const { sceneContext } = req.body;

        const prompt = `Based on this scene context: "${sceneContext || "Action scene in movie"}", generate 1 multiple choice quiz question with 4 options and mark the correct answer. Format as JSON.`;

        const response = await ai.models.generateContent({
            model: 'gemini-2.5-flash',
            contents: prompt,
        });

        res.json({ success: true, quiz: response.text });
    } catch (error) {
        console.error("Gemini Quiz Error:", error);
        res.status(500).json({ success: false, error: "Failed to generate quiz." });
    }
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => {
    console.log(`FirePulse backend running on http://localhost:${PORT}`);
});