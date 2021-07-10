package me.laotang.carry.mvvm.binding

import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class CommandDelegate<T : Any>(
    private val name: String,
    private val hookEnable: Boolean,
    private val commandHook: CommandHook?,
    private val block: (T) -> Unit
) :
    ReadOnlyProperty<Any, Command<T>> {
    override fun getValue(thisRef: Any, property: KProperty<*>): Command<T> {
        return CommandHookOwner(
            name = name,
            hookEnable = hookEnable,
            commandHook = commandHook,
            block = block,
            commandName = property.name
        )
    }
}

fun <T : Any> command(
    name: String = "",
    hookEnable: Boolean = false,
    commandHook: CommandHook? = null,
    block: (T) -> Unit
): CommandDelegate<T> =
    CommandDelegate(name = name, hookEnable = hookEnable, commandHook = commandHook, block = block)


internal class CommandHookOwner<T : Any>(
    name: String = "",
    commandName: String,
    hookEnable: Boolean,
    commandHook: CommandHook?,
    private val block: (T) -> Unit,
) :
    Command<T> {

    private var mCommandHook: CommandHook? = null
    private var mName: String = ""

    init {
        if (hookEnable) {
            mCommandHook = commandHook ?: CommandHook.defaultCommandCommandHook
        }
        mName = if (name.isNotEmpty()) name else commandName
    }

    override fun execute(t: T) {
        if (mCommandHook?.beforeInvoke(mName, t) == true) {
            return
        }
        block.invoke(t)
        mCommandHook?.afterInvoke(mName, t)
    }

}

abstract class CommandHook {
    open fun beforeInvoke(commandName: String, params: Any): Boolean {
        return false
    }

    open fun afterInvoke(commandName: String, params: Any) {

    }

    companion object {
        internal var defaultCommandCommandHook: CommandHook? = null

        @Synchronized
        fun setDefaultHook(commandHook: CommandHook) {
            defaultCommandCommandHook = commandHook
        }
    }
}

