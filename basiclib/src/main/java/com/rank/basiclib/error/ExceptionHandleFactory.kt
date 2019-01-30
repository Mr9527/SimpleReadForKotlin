package com.rank.basiclib.error

import android.annotation.SuppressLint
import android.app.Application
import android.widget.Toast
import com.google.gson.JsonIOException
import com.google.gson.JsonParseException
import com.rank.basiclib.data.AppResponse
import com.rank.basiclib.ext.application
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.json.JSONException
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.ParseException

/**
 * <pre>
 *     author: ChenZhaoJun
 *     url  :
 *     time  : 2018/8/8
 *     desc  :
 * </pre>
 */
class ExceptionHandleFactory(application: Application) {


    companion object {
        private const val TAG = "Error-Log"
        private const val SERVICE_ERROR = "服务器发生错误"
        private const val LOCATION_ERROR = "请求地址不存在"
        private const val SERVICE_ACCESS_DENIED_ERROR = "服务器拒绝访问"
        private const val REQUEST_REDIRECT = "请求被重定向"
        private const val DATA_ANALYSIS_ERROR = "数据解析错误"
        private const val NETWORK_ERROR = "网络错误，请连接网络后重试"
        private const val NETWORK_TIME_OUT_ERROR = "网络连接超时，请确认网络连接环境"


        private fun convertStatusCode(httpException: HttpException): String {
            return when {
                httpException.code() == 500 -> SERVICE_ERROR
                httpException.code() == 404 -> LOCATION_ERROR
                httpException.code() == 403 -> SERVICE_ACCESS_DENIED_ERROR
                httpException.code() == 307 -> REQUEST_REDIRECT
                else -> "未知错误 错误码${httpException.code()}"
            }
        }

        @SuppressLint("CheckResult")
        public fun handlerResponseError(t: Throwable) {
            val message = when (t) {
                is ServiceException -> handlerServiceException(t)
                is HttpException -> convertStatusCode(t)
                is UnknownHostException -> NETWORK_ERROR
                is SocketTimeoutException -> NETWORK_TIME_OUT_ERROR
                is JsonParseException, is ParseException, is JSONException, is JsonIOException -> DATA_ANALYSIS_ERROR
                else -> t.message
            }
            Observable.just(message)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        Toast.makeText(application(), it, Toast.LENGTH_SHORT).show() }
        }

        private fun handlerServiceException(t: ServiceException): String {

            return t.message
        }


        fun <T : AppResponse<*>> interceptorError(errorHandler: () -> Unit = {}): GlobalErrorTransformer<T, T> = GlobalErrorTransformer(
                onNextInterceptor = interceptorError(),
                onErrorResumeNext = defaultErrorReturn(),
                onErrorConsumer = defaultErrorConsumer(errorHandler),
                onErrorRetrySupplier = defaultErrorRetrySupplier())

        fun <T : AppResponse<K>, K> convertData(errorHandler: () -> Unit = {}): GlobalErrorTransformer<T, K> = GlobalErrorTransformer(
                onNextInterceptor = interceptorErrorAndShellData(),
                onErrorResumeNext = convertErrorReturn(),
                onErrorConsumer = defaultErrorConsumer(errorHandler),
                onErrorRetrySupplier = defaultErrorRetrySupplier())


        private fun <T : AppResponse<K>, K> interceptorErrorAndShellData(): (T) -> Observable<K> {
            return {
                if (it.success()) {
                    Observable.just(it.data())
                } else {
                    Observable.error(ServiceException(it))
                }
            }
        }

        private fun <T : AppResponse<K>, K> convertErrorReturn(): (Throwable) -> Observable<K> {
            return {
                Observable.empty<K>()
            }
        }

        private fun <T : AppResponse<*>> interceptorError(): (T) -> Observable<T> {
            return {
                if (it.success()) {
                    Observable.just(it)
                } else {
                    Observable.error(ServiceException(it))
                }
            }
        }

        private fun <T : AppResponse<*>> defaultErrorReturn(): (Throwable) -> Observable<T> {
            return {
                Observable.empty<T>()
            }
        }

        private fun defaultErrorRetrySupplier(): (Throwable) -> RetryConfig {
            return {
                RetryConfig.none()
            }
        }

        private fun defaultErrorConsumer(errorHandler: () -> Unit): (Throwable) -> Unit {
            return {
                handlerResponseError(it)
                errorHandler()
            }
        }

    }
}