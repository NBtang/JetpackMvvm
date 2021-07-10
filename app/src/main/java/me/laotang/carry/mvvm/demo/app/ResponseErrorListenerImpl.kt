package me.laotang.carry.mvvm.demo.app

import android.content.Context
import android.net.ParseException
import com.google.gson.JsonIOException
import com.google.gson.JsonParseException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.jessyan.rxerrorhandler.handler.listener.ResponseErrorListener
import me.laotang.carry.core.http.response.ErrorReport
import me.laotang.carry.mvvm.demo.R
import me.laotang.carry.util.toasty
import org.json.JSONException
import retrofit2.HttpException
import timber.log.Timber
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * ================================================
 * 展示 [ResponseErrorListener] 的用法
 * ================================================
 */
class ResponseErrorListenerImpl : ResponseErrorListener {

    override fun handleResponseError(context: Context, t: Throwable) {
        //这里不光只能打印错误, 还可以根据不同的错误做出不同的逻辑处理
        //这里只是对几个常用错误进行简单的处理, 展示这个类的用法, 在实际开发中请您自行对更多错误进行更严谨的处理
        Timber.e(t)
        var msg = context.getString(R.string.unknow_error)
        if (t is UnknownHostException) {
            msg = context.getString(R.string.network_unable)
        } else if (t is SocketTimeoutException) {
            msg = context.getString(R.string.request_network_unable)
        } else if (t is HttpException) {
            msg = convertStatusCode(t, context)
        } else if (t is JsonParseException || t is ParseException || t is JSONException || t is JsonIOException) {
            msg = context.getString(R.string.data_parse_error)
        } else if(t is ConnectException){
            msg = context.getString(R.string.request_rejusted_service)
        } else if (t is ErrorReport) {
            GlobalScope.launch(Dispatchers.Main) {
                t.message?.toasty()
            }
            return
        }
        msg.toasty()
    }

    private fun convertStatusCode(httpException: HttpException, context: Context): String {
        return when {
            httpException.code() == 500 -> context.getString(R.string.service_happen_error)
            httpException.code() == 404 -> context.getString(R.string.request_address_unexit)
            httpException.code() == 403 -> context.getString(R.string.request_rejusted_service)
            httpException.code() == 307 -> context.getString(R.string.request_redirect_other_page)
            else -> httpException.message()
        }
    }
}
