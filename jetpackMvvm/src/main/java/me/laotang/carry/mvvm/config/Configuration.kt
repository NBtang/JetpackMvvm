package me.laotang.carry.mvvm.config

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import me.laotang.carry.di.CustomConfiguration
import me.laotang.carry.di.GlobalConfigModule
import me.laotang.carry.mvvm.binding.CommandHook

fun GlobalConfigModule.Builder.commandHook(commandHook: CommandHook): GlobalConfigModule.Builder {
    this.customConfiguration(CommandHookConfiguration(commandHook))
    return this
}

class CommandHookConfiguration(val commandHook: CommandHook) : CustomConfiguration


@EntryPoint
@InstallIn(ApplicationComponent::class)
interface ConfigurationEntryPoint {
    fun globalConfigModule(): GlobalConfigModule
}