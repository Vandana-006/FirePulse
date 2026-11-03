const express = require('express');
const cors = require('cors');

const app = express();
app.use(express.json());
app.use(cors());

async function callOpenRouter(prompt) {
    const apiKey = process.env.OPENROUTER_API_KEY;
    if (!apiKey) {
        throw new Error("OPENROUTER_API_KEY is missing!");
    }

    const response = await fetch('https://openrouter.ai/api/v1/chat/completions', {
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${apiKey.trim()}`,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            model: 'openrouter/free',
            messages: [{ role: 'user', content: prompt }]
        })
    });

    const data = await response.json();

    if (!response.ok) {
        console.error('OpenRouter Error:', JSON.stringify(data));
        throw new Error(data.error?.message || `HTTP ${response.status} Error`);
    }

    return data.choices?.[0]?.message?.content || "No response received.";
}

app.post('/api/ask', async (req, res) => {
    try {
        const { question, sceneContext } = req.body;
        const prompt = `Scene Context: "${sceneContext}"\nUser Question: "${question}"\nProvide a concise and engaging answer for a TV viewer.`;
        const answer = await callOpenRouter(prompt);
        res.json({ answer });
    } catch (error) {
        console.error('Ask Endpoint Error:', error.message);
        res.status(500).json({ error: error.message });
    }
});

app.post('/api/quiz', async (req, res) => {
    try {
        const { sceneContext } = req.body;
        const prompt = `Based on scene context: "${sceneContext}", generate 1 fun multiple-choice trivia question with 4 options and the correct answer.`;
        const quiz = await callOpenRouter(prompt);
        res.json({ quiz });
    } catch (error) {
        console.error('Quiz Endpoint Error:', error.message);
        res.status(500).json({ error: error.message });
    }
});

const PORT = process.env.PORT || 5000;
app.listen(PORT, () => console.log(`FirePulse OpenRouter Backend active on port ${PORT}`));