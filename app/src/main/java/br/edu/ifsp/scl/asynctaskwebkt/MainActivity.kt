package br.edu.ifsp.scl.asynctaskwebkt

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Thread.sleep
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {

    companion object {
        const val URL_BASE = "http://www.nobile.pro.br/sdm/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonSearch.setOnClickListener {
            val searchMessageAsyncTask = SearchMessageAsyncTask()

            searchMessageAsyncTask.execute(URL_BASE + "texto.php")
//            searchMessageAsyncTask.execute(URL_BASE + "data.php")
            searchData(URL_BASE + "data.php")
        }

    }
    @SuppressLint("StaticFieldLeak")
    private inner class SearchMessageAsyncTask : AsyncTask<String, Int, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            toast("Buscando String no Web Service")
            progressBar?.visibility = View.VISIBLE
        }
        override fun doInBackground(vararg params: String?): String {
            val url = params[0]
            val stringBufferResponse = StringBuffer()
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val bufferedReader = BufferedInputStream(inputStream).bufferedReader()
                    val responseList = bufferedReader.readLines()
                    responseList.forEach { stringBufferResponse.append(it) }
                }
            } catch (ioe: IOException) {
                toast("Erro na conexão!")
            }
            for (i in 1..10) {
                publishProgress(i)
                sleep(500)
            }
            return stringBufferResponse.toString()
        }
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            toast("Texto recuperado com sucesso")
            textViewMessage.text = result
            progressBar?.visibility = View.GONE
        }
        override fun onProgressUpdate(vararg values: Int?) {
            values[0]?.apply { progressBar?.progress = this }
        }
    }

    private fun toast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun searchData(url: String) {
        val searchDataAsyncTask = @SuppressLint("StaticFieldLeak")
        object : AsyncTask<String, Void, JSONObject>() {
            override fun onPreExecute() {
                super.onPreExecute()
                progressBar.visibility = View.VISIBLE
            }

            override fun doInBackground(vararg strings: String): JSONObject? {
                var jsonObject: JSONObject? = null
                val sb = StringBuilder()
                try {
                    val url = strings[0]
                    val connection = URL(url).openConnection() as HttpURLConnection
                    if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                        val iss = connection.inputStream
                        val br = BufferedReader(InputStreamReader(iss))
                        for (temp in br.readLine()) {
                            sb.append(temp)
                        }
                    }
                    jsonObject = JSONObject(sb.toString())
                } catch (ioE: IOException) {
                    ioE.printStackTrace()
                } catch (jsonE: JSONException) {
                    jsonE.printStackTrace()
                }

                return jsonObject
            }

            override fun onPostExecute(s: JSONObject) {
                var date: String? = null
                var hour: String? = null
                var ds: String? = null
                super.onPostExecute(s)
                try {
                    date = s.getInt("mday").toString() + "/" + s.getInt("mon") + "/" + s.getInt("year")
                    hour = s.getInt("hours").toString() + ":" + s.getInt("minutes") + ":" + s.getInt("seconds")
                    ds = s.getString("weekday")
                } catch (jsone: JSONException) {
                    jsone.printStackTrace()
                }

                (textViewDate as TextView).text = date + "\n" + hour + "\n" + ds
                progressBar.visibility = View.GONE
            }
        }
        searchDataAsyncTask.execute(url)
    }

}