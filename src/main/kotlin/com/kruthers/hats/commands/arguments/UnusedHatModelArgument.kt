package com.kruthers.hats.commands.arguments

import cloud.commandframework.ArgumentDescription
import cloud.commandframework.arguments.CommandArgument
import cloud.commandframework.arguments.parser.ArgumentParseResult
import cloud.commandframework.arguments.parser.ArgumentParser
import cloud.commandframework.captions.Caption
import cloud.commandframework.captions.CaptionVariable
import cloud.commandframework.context.CommandContext
import cloud.commandframework.exceptions.parsing.NoInputProvidedException
import cloud.commandframework.exceptions.parsing.NumberParseException
import cloud.commandframework.exceptions.parsing.ParserException
import com.kruthers.hats.HatManager
import java.util.*
import java.util.function.BiFunction

class UnusedHatModelArgument<C: Any>(
    required: Boolean,
    name: String,
    defaultValue: String,
    suggestionsProvider: BiFunction<CommandContext<C>, String, MutableList<String>>?,
    defaultDescription: ArgumentDescription
) : CommandArgument<C, Int>(
    required,
    name,
    UnusedHatModelParser(),
    defaultValue,
    Int::class.java,
    suggestionsProvider,
    defaultDescription
) {

    companion object {
        fun <C : Any> newBuilder(name: String): Builder<C> {
            return Builder(name)
        }

        fun <C : Any> of(name: String): CommandArgument<C, Int> {
            return newBuilder<C>(name).asRequired().build()
        }

        @Suppress("UNUSED")
        fun <C: Any> optional(name: String): CommandArgument<C, Int> {
            return newBuilder<C>(name).asOptional().build()
        }

        @Suppress("UNUSED")
        fun <C: Any> optional(name: String, defaultPlayer: String): CommandArgument<C, Int> {
            return newBuilder<C>(name).asOptionalWithDefault(defaultPlayer).build()
        }

        class Builder<C : Any> (name: String) : CommandArgument.Builder<C, Int>(Int::class.java, name) {
            override fun build(): CommandArgument<C, Int> {
                return UnusedHatModelArgument(this.isRequired, this.name, this.defaultValue,
                    this.suggestionsProvider, this.defaultDescription
                )
            }
        }

        class UnusedHatModelParser<C: Any>(): ArgumentParser<C, Int> {
            override fun parse(
                commandContext: CommandContext<C>,
                inputQueue: Queue<String>
            ): ArgumentParseResult<Int> {
                val input = inputQueue.peek()
                    ?: return ArgumentParseResult.failure(NoInputProvidedException(this::class.java, commandContext))


                val value: Int = try {
                    val number = input.toInt()
                    if (number < 0) {
                        return ArgumentParseResult.failure(InvalidNumberException(commandContext, input))
                    }
                    inputQueue.remove()
                    number
                } catch (e: Exception) {
                    return ArgumentParseResult.failure(InvalidNumberException(commandContext, input))
                }

                val hat = HatManager.getHatFromModelData(value)
                return if (hat == null) {
                    ArgumentParseResult.success(value)
                } else {
                    ArgumentParseResult.failure(ModelIdNotUniteException(input, commandContext))
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

        class ModelIdNotUniteException(input: String, context: CommandContext<*>) : ParserException(
            UnusedHatModelParser::class.java,
            context,
            Caption.of("hats.model_id_not_unite"),
            CaptionVariable.of("input", input)
        )

        class InvalidNumberException(
            context: CommandContext<*>,
            input: String
        ): NumberParseException(
            input,
            0,
            Int.MAX_VALUE,
            UnusedHatModelParser::class.java,
            context
        ) {
            /**
             * Get the number type
             *
             * @return Number type
             */
            override fun getNumberType(): String {
                return "integer"
            }

            /**
             * If the parser had a maximum value
             *
             * @return `true` if there was a maximum value, else `false`
             */
            override fun hasMax(): Boolean {
                return true
            }

            /**
             * If the parser had a minimum value
             *
             * @return `true` if there was a minimum value, else `false`
             */
            override fun hasMin(): Boolean {
                return true
            }

        }
    }


}