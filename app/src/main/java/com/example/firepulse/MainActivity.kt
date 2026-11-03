package com.example.firepulse

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : FragmentActivity() {

    private lateinit var tvSceneContext: TextView
    private lateinit var btnAskAI: Button
    private lateinit var btnStartQuiz: Button

    private val client = OkHttpClient()
    private val baseUrl = "http://10.0.2.2:5000/api"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvSceneContext = findViewById(R.id.tvSceneContext)
        btnAskAI = findViewById(R.id.btnAskAI)
        btnStartQuiz = findViewById(R.id.btnStartQuiz)

        tvSceneContext.text = "FirePulse Active: Listening to scene audio stream..."

        btnAskAI.setOnClickListener {
            showAskAiDialog()
        }

        btnStartQuiz.setOnClickListener {
            handleStartQuiz()
        }
    }

    private fun showAskAiDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Ask Gemini Anything")

        val input = EditText(this)
        input.hint = "e.g., What is happening in this scene?"
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("Ask") { dialog, _ ->
            val userQuery = input.text.toString().trim()
            if (userQuery.isNotEmpty()) {
                sendQuestionToBackend(userQuery)
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun sendQuestionToBackend(userQuestion: String) {
        tvSceneContext.text = "Asking Gemini AI..."
        Toast.makeText(this, "Querying Gemini AI...", Toast.LENGTH_SHORT).show()

        val json = JSONObject().apply {
            put("question", userQuestion)
            put("sceneContext", "Hero steps into the dark alley looking for clues.")
        }

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("$baseUrl/ask")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    tvSceneContext.text = "Connection Failed: ${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    if (response.isSuccessful && responseData != null) {
                        val jsonResponse = JSONObject(responseData)
                        val answer = jsonResponse.optString("answer", "No answer received.")
                        tvSceneContext.text = answer
                    } else {
                        tvSceneContext.text = "Error: Server returned ${response.code}"
                    }
                }
            }
        })
    }

    private fun handleStartQuiz() {
        tvSceneContext.text = "Generating Quiz..."
        Toast.makeText(this, "Fetching Quiz...", Toast.LENGTH_SHORT).show()

        val json = JSONObject().apply {
            put("sceneContext", "Hero steps into the dark alley looking for clues.")
        }

        val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url("$baseUrl/quiz")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    tvSceneContext.text = "Connection Failed: ${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                runOnUiThread {
                    if (response.isSuccessful && responseData != null) {
                        val jsonResponse = JSONObject(responseData)
                        val quiz = jsonResponse.optString("quiz", "No quiz generated.")
                        tvSceneContext.text = quiz
                    } else {
                        tvSceneContext.text = "Error: Server returned ${response.code}"
                    }
                }
            }
        })
    }
}