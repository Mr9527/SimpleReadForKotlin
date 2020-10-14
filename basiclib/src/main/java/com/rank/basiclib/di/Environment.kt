package com.rank.basiclib.di

import androidx.lifecycle.ViewModelProvider
import com.rank.basiclib.error.ServiceErrorHandler
import com.rank.basiclib.log.GlobalHttpHandler
import dagger.Module
import dagger.Provides
import okhttp3.Authenticator
import okhttp3.Interceptor
import javax.inject.Singleton

/**
 * <pre>
 *     author: ChenZhaoJun
 *     url  :
 *     time  : 2019/1/23
 *     desc  :
 * </pre>
 */
@Module
class EnvironmentModule {
    lateinit var factory: ViewModelProvider.Factory
    lateinit var handler: GlobalHttpHandler
    lateinit var serviceErrorHandlers: MutableList<ServiceErrorHandler>
    lateinit var interceptors: ArrayList<Interceptor>
    var authenticator: Authenticator? = null

    @Singleton
    @Provides
    fun providerFactory() = factory

    @Provides
    @Singleton
    fun providerHttpHandler() = handler

    @Provides
    @Singleton
    fun providerServiceErrorHandler() = serviceErrorHandlers

    @Provides
    @Singleton
    fun providerInterceptor(handler: GlobalHttpHandler) = interceptors

    @Provides
    @Singleton
    fun providerAuthenticator(): Authenticator? = authenticator
}