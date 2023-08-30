package com.kruthers.hats.commands.arguments

import cloud.commandframework.ArgumentDescription
import cloud.commandframework.arguments.CommandArgument
import cloud.commandframework.arguments.parser.ArgumentParseResult
import cloud.commandframework.arguments.parser.ArgumentParser
import cloud.commandframework.captions.Caption
import cloud.commandframework.captions.CaptionVariable
import cloud.commandframework.context.CommandContext
import cloud.commandframework.exceptions.parsing.NoInputProvidedException
import cloud.commandframework.exceptions.parsing.ParserException
import com.kruthers.hats.HatsPlugin
import com.kruthers.hats.classes.Hat
import java.util.*
import java.util.function.BiFunction

class HatArgument<C: Any>(
    required: Boolean,
    name: String,
    defaultValue: String,
    suggestionsProvider: BiFunction<CommandContext<C>, String, MutableList<String>>?,
    defaultDescription: ArgumentDescription
) : CommandArgument<C, Hat>(
    required,
    name,
    HatParser(),
    defaultValue,
    Hat::class.java,
    suggestionsProvider,
    defaultDescription
) {

    companion object {
        fun <C : Any> newBuilder(name: String): Builder<C> {
            return Builder(name)
        }

        fun <C : Any> of(name: String): CommandArgument<C, Hat> {
            return newBuilder<C>(name).asRequired().build()
        }

        @Suppress("UNUSED")
        fun <C: Any> optional(name: String): CommandArgument<C, Hat> {
            return newBuilder<C>(name).asOptional().build()
        }

        @Suppress("UNUSED")
        fun <C: Any> optional(name: String, defaultPlayer: String): CommandArgument<C, Hat> {
            return newBuilder<C>(name).asOptionalWithDefault(defaultPlayer).build()
        }

        class Builder<C : Any> (name: String) : CommandArgument.Builder<C, Hat>(Hat::class.java, name) {
            override fun build(): CommandArgument<C, Hat> {
                return HatArgument(this.isRequired, this.name, this.defaultValue,
                    this.suggestionsProvider, this.defaultDescription
                )
            }
        }

        class HatParser<C: Any>(): ArgumentParser<C, Hat> {
            override fun parse(
                commandContext: CommandContext<C>,
                inputQueue: Queue<String>
            ): ArgumentParseResult<Hat> {
                val input = inputQueue.peek()
                    ?: return ArgumentParseResult.failure(NoInputProvidedException(this::class.java, commandContext))

                val hat = HatsPlugin.hats[input]
                return if (hat != null) {
                    inputQueue.remove()
                    ArgumentParseResult.success(hat)
                } else {
                    ArgumentParseResult.failure(HatNotFoundException(input, commandContext))
                }


            }

            override fun suggestions(commandContext: CommandContext<C>, input: String): MutableList<String> {
                val suggestions: MutableList<String> = mutableListOf()
                for (i in 0..9) {
                    suggestions.add("${input}${i}")
                }
                return suggestions
            }
        }

        class HatNotFoundException(input: String, context: CommandContext<*>) : ParserException(
            HatParser::class.java,
            context,
            Caption.of("hats.hat_not_found"),
            CaptionVariable.of("input", input)
        )
    }


}