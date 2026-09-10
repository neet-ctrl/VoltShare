package com.twofasapp.feature.secrets.di

import com.twofasapp.common.di.KoinModule
import com.twofasapp.common.domain.SecretsBackupProvider
import com.twofasapp.feature.secrets.data.SecretsRepository
import com.twofasapp.feature.secrets.ui.SecretsViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

class SecretsModule : KoinModule {
    override fun provide() = module {
        single {
            SecretsRepository(
                androidContext = get(),
                json = get(),
                cloudSyncWorkDispatcher = get(),
            )
        }
        single<SecretsBackupProvider> { get<SecretsRepository>() }
        viewModelOf(::SecretsViewModel)
    }
}