package com.rank.basiclib.http

import android.util.LruCache
import com.rank.basiclib.log.GlobalHttpHandler
import dagger.internal.Preconditions
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import javax.inject.Inject
import javax.inject.Singleton

/**
 * <pre>
 *     author: ChenZhaoJun
 *     url  :
 *     time  : 2019/1/17
 *     desc  :
 * </pre>
 */
@Singleton
class NetworkManager
@Inject constructor()
    : GlobalHttpHandler {

    @Inject
    lateinit var retrofit: Retrofit

    private val mRetrofitServiceCache: LruCache<String, Any> = LruCache(1024 * 5)

    /**
     * 根据传入的 Class 获取对应的 Retrofit service
     *
     * @param serviceClass ApiService class
     * @param <T>          ApiService class
     * @return ApiService
    </T> */
    @Synchronized
    fun <T : Any> load(serviceClass: Class<T>): T {
        return createWrapperService(serviceClass)
    }

    /**
     * 根据 https://zhuanlan.zhihu.com/p/40097338 对 Retrofit 进行的优化
     *
     * @param serviceClass ApiService class
     * @param <T>          ApiService class
     * @return ApiService
    </T> */
    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> createWrapperService(serviceClass: Class<T>): T {
        // 通过二次代理，对 Retrofit 代理方法的调用包进新的 Observable 里在 io 线程执行。
        return Proxy.newProxyInstance(serviceClass.classLoader,
                arrayOf(serviceClass), object : InvocationHandler {
            override fun invoke(proxy: Any, method: Method, args: Array<out Any>?): Any {

                if (method.returnType == Observable::class.java) {
                    return Observable.defer {
                        val service = getRetrofitService(serviceClass)
                        // 执行真正的 Retrofit 动态代理的方法
                        (getRetrofitMethod(service, method)
                                .invoke(service, *getParams(args)) as Observable<*>)
                                .subscribeOn(Schedulers.io())
                    }.subscribeOn(Schedulers.single())
                }
                val service = getRetrofitService(serviceClass)
                return getRetrofitMethod(service, method).invoke(service, *getParams(args))
            }
        }) as T
    }

    private fun getParams(args: Array<out Any>?): Array<out Any?> {
        return args ?: arrayOfNulls<Any>(0)
    }

    /**
     * 根据传入的 Class 获取对应的 Retrofit service
     *
     * @param serviceClass ApiService class
     * @param <T>          ApiService class
     * @return ApiService
    </T> */
    private fun <T> getRetrofitService(serviceClass: Class<T>): T {
        Preconditions.checkNotNull(mRetrofitServiceCache,
                "Cannot return null from a Cache.Factory#build(int) method")
        var retrofitService: T = mRetrofitServiceCache.get(serviceClass.canonicalName) as T
        if (retrofitService == null) {
            retrofitService = retrofit.create(serviceClass)
            mRetrofitServiceCache.put(serviceClass.canonicalName, retrofitService)
        }
        return retrofitService
    }

    @Throws(NoSuchMethodException::class)
    private fun <T : Any> getRetrofitMethod(service: T, method: Method): Method {
        return service::class.java.getMethod(method.name, *method.parameterTypes)
    }

    override fun onHttpResultResponse(httpResult: String?, chain: Interceptor.Chain, response: Response): Response {
        return response
    }

    override fun onHttpRequestBefore(chain: Interceptor.Chain, request: Request): Request {

        return request
    }
}